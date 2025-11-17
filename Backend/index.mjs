import axios from 'axios';
import dotenv from 'dotenv';
import crypto from 'crypto';

dotenv.config();

const KICK_AUTH_URL  = 'https://id.kick.com/oauth/authorize';
const KICK_TOKEN_URL = 'https://id.kick.com/oauth/token';

const TWITCH_AUTH_URL  = 'https://id.twitch.tv/oauth2/authorize';
const TWITCH_TOKEN_URL = 'https://id.twitch.tv/oauth2/token';
const TWITCH_USER_URL  = 'https://api.twitch.tv/helix/users';

export const handler = async (event) => {
  const path  = event.rawPath;
  const query = event.queryStringParameters ?? {};

  /* ─────────────────────────────────────────────
   * 1)  /auth/kick/start   → redirect do Kick
   * ────────────────────────────────────────────*/
  if (path === '/auth/kick/start') {
    const { username } = query;

    const codeVerifier  = crypto.randomBytes(64).toString('hex');
    const codeChallenge = crypto.createHash('sha256').update(codeVerifier).digest('base64')
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    const state = crypto.randomBytes(16).toString('hex');

    const redirectUrl =
      `${KICK_AUTH_URL}?response_type=code` +
      `&client_id=${process.env.KICK_CLIENT_ID}` +
      `&redirect_uri=${encodeURIComponent(process.env.KICK_REDIRECT_URI)}` +
      `&scope=${encodeURIComponent('user:read channel:read')}` +
      `&state=${state}` +
      `&code_challenge=${codeChallenge}` +
      `&code_challenge_method=S256`;

    const cookieFlags = 'HttpOnly; Path=/; Max-Age=300; Secure; SameSite=Lax';
    const cookies = [
      `state=${state}; ${cookieFlags}`,
      `code_verifier=${codeVerifier}; ${cookieFlags}`
    ];
    if (username) cookies.push(`username=${encodeURIComponent(username)}; ${cookieFlags}`);

    return {
      statusCode: 302,
      headers: { Location: redirectUrl },
      cookies,
      body: ''
    };
  }

  /* ─────────────────────────────────────────────
   * 2)  /auth/kick/callback  → exchange + deep-link
   * ────────────────────────────────────────────*/
  if (path === '/auth/kick/callback') {
    const { code, state } = query;
    const cookieMap = {};
    for (const rawCookie of event.cookies ?? []) {
      const [pair] = rawCookie.split(';');
      if (!pair) continue;
      const [key, ...rest] = pair.split('=');
      if (!key) continue;
      cookieMap[key.trim()] = rest.join('=');
    }

    if (!code || !state || cookieMap.state !== state || !cookieMap.code_verifier) {
      return { statusCode: 400, body: 'Invalid code, state or verifier' };
    }

    try {
      const tokenRes = await axios.post(
        KICK_TOKEN_URL,
        new URLSearchParams({
          grant_type: 'authorization_code',
          code,
          client_id:     process.env.KICK_CLIENT_ID,
          client_secret: process.env.KICK_CLIENT_SECRET,
          redirect_uri:  process.env.KICK_REDIRECT_URI,
          code_verifier: cookieMap.code_verifier
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );
      const accessToken = tokenRes.data.access_token;

      let channelId = '';
      const username = cookieMap.username ? decodeURIComponent(cookieMap.username) : undefined;
      if (username) {
        try {
          const { data } = await axios.get('https://api.kick.com/public/v1/channels', {
            params: { slug: username },
            headers: {
              Authorization: `Bearer ${accessToken}`,
              Accept: 'application/json'
            }
          });
          channelId =
            data?.data?.[0]?.broadcaster_user_id?.toString() ??
            data?.data?.[0]?.id?.toString() ??
            '';
        } catch (err) {
          console.error('Kick channel lookup failed', err?.response?.data ?? err?.message);
        }
      }

      const mobileRedirect =
        `irlmate://auth/kick/callback` +
        `?access_token=${encodeURIComponent(accessToken)}` +
        `&refresh_token=${encodeURIComponent(tokenRes.data.refresh_token)}` +
        `&expires_in=${tokenRes.data.expires_in}` +
        `&token_type=${tokenRes.data.token_type}` +
        `&scope=${encodeURIComponent(tokenRes.data.scope ?? '')}` +
        `&channel_id=${encodeURIComponent(channelId)}`;

      return {
        statusCode: 302,
        headers: { Location: mobileRedirect },
        body: ''
      };
    } catch (err) {
      console.error('Kick token exchange failed', err?.response?.data ?? err?.message);
      return {
        statusCode: 500,
        body: JSON.stringify({ error: 'Kick token exchange failed', details: err?.message })
      };
    }
  }

  /* ─────────────────────────────────────────────
   * 3)  /auth/twitch/start   → redirect do Twitch
   * ────────────────────────────────────────────*/
  if (path === '/auth/twitch/start') {
    const codeVerifier  = crypto.randomBytes(64).toString('hex');
    const codeChallenge = crypto.createHash('sha256').update(codeVerifier).digest('base64')
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    const state = crypto.randomBytes(16).toString('hex');

    // Scopes zgodne z dokumentacją Twitch: lista rozdzielona spacją, potem URL-enkodowana
    const twitchScope = encodeURIComponent('chat:read user:read:email');

    const redirectUrl =
      `${TWITCH_AUTH_URL}?response_type=code` +
      `&client_id=${process.env.TWITCH_CLIENT_ID}` +
      `&redirect_uri=${encodeURIComponent(process.env.TWITCH_REDIRECT_URI)}` +
      `&scope=${twitchScope}` +
      `&state=${state}` +
      `&code_challenge=${codeChallenge}` +
      `&code_challenge_method=S256`;

    // Ujednolicone flagi cookies – tak jak dla Kick
    const cookieFlags = 'HttpOnly; Path=/; Max-Age=300; Secure; SameSite=Lax';
    const cookies = [
      `state=${state}; ${cookieFlags}`,
      `code_verifier=${codeVerifier}; ${cookieFlags}`
    ];

    return {
      statusCode: 302,
      headers: { Location: redirectUrl },
      cookies,
      body: ''
    };
  }

  /* ─────────────────────────────────────────────
   * 4)  /auth/twitch/callback  → exchange + user info + redirect
   * ────────────────────────────────────────────*/
  if (path === '/auth/twitch/callback') {
    const { code, state } = query;
    const cookieMap = Object.fromEntries(
      (event.cookies ?? []).map(c => c.split('='))
    );

    if (!code || !state || cookieMap.state !== state || !cookieMap.code_verifier) {
      return { statusCode: 400, body: 'Invalid code, state or verifier' };
    }

    try {
      const tokenRes = await axios.post(
        TWITCH_TOKEN_URL,
        new URLSearchParams({
          grant_type: 'authorization_code',
          code,
          client_id:     process.env.TWITCH_CLIENT_ID,
          client_secret: process.env.TWITCH_CLIENT_SECRET,
          redirect_uri:  process.env.TWITCH_REDIRECT_URI,
          code_verifier: cookieMap.code_verifier
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );

      const accessToken = tokenRes.data.access_token;
      const refreshToken = tokenRes.data.refresh_token;
      const expiresIn = tokenRes.data.expires_in; // <--- POBIERAMY expires_in!
      const userRes = await axios.get(TWITCH_USER_URL, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Client-Id': process.env.TWITCH_CLIENT_ID
        }
      });

      const user = userRes.data?.data?.[0];
      if (!user?.id || !user?.login) {
        return { statusCode: 500, body: 'Missing Twitch user info' };
      }

      const mobileRedirect =
        `irlmate://auth/twitch/callback` +
        `?access_token=${encodeURIComponent(accessToken)}` +
        `&refresh_token=${encodeURIComponent(refreshToken)}` +
        `&expires_in=${expiresIn}` +    // <--- DODAJEMY expires_in do deep-linka!
        `&user_id=${encodeURIComponent(user.id)}` +
        `&username=${encodeURIComponent(user.login)}`;

      return {
        statusCode: 302,
        headers: { Location: mobileRedirect },
        body: ''
      };
    } catch (err) {
      return {
        statusCode: 500,
        body: JSON.stringify({ error: 'Twitch token exchange failed', details: err?.message })
      };
    }
  }

  /* ─────────────────────────────────────────────
   * 5)  /auth/kick/refresh  → Refresh Kick token
   * ────────────────────────────────────────────*/
  if (path === '/kick/messages') {
    const broadcasterId = query.broadcaster_id ?? query.broadcasterId ?? query.id;
    if (!broadcasterId) {
      return { statusCode: 400, body: 'Missing broadcaster_id' };
    }
    const items = kickChatMessages.get(String(broadcasterId)) ?? [];
    return {
      statusCode: 200,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ items })
    };
  }

  if (path === '/auth/kick/subscribe') {
    const method = event.requestContext?.http?.method ?? event.requestContext?.httpMethod ?? 'POST';
    if (method !== 'POST') {
      return { statusCode: 405, body: 'Method Not Allowed' };
    }
    const body = parseBody(event);
    const accessToken = body?.access_token ?? body?.accessToken;
    const broadcasterId = body?.broadcaster_id ?? body?.broadcasterId;
    if (!accessToken || !broadcasterId) {
      return { statusCode: 400, body: 'Missing access_token or broadcaster_id' };
    }
    try {
      await axios.post(
        'https://api.kick.com/public/v1/events/subscriptions',
        {
          events: [
            {
              type: 'chat.message.sent',
              version: 1,
              condition: { broadcaster_user_id: String(broadcasterId) }
            }
          ]
        },
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
            Accept: 'application/json'
          }
        }
      );
      return { statusCode: 200, body: JSON.stringify({ ok: true }) };
    } catch (err) {
      console.error('Kick subscribe failed', err?.response?.data ?? err?.message);
      return {
        statusCode: 500,
        body: JSON.stringify({
          error: 'Kick subscribe failed',
          details: err?.response?.data ?? err?.message
        })
      };
    }
  }

  if (path === '/auth/kick/refresh') {
    const refresh_token = query.refresh_token || getFromBody(event, 'refresh_token');
    if (!refresh_token) {
      return { statusCode: 400, body: 'Missing refresh_token' };
    }
    try {
      const tokenRes = await axios.post(
        KICK_TOKEN_URL,
        new URLSearchParams({
          grant_type: 'refresh_token',
          refresh_token,
          client_id: process.env.KICK_CLIENT_ID,
          client_secret: process.env.KICK_CLIENT_SECRET
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );
      return {
        statusCode: 200,
        body: JSON.stringify(tokenRes.data)
      };
    } catch (err) {
      return {
        statusCode: 500,
        body: JSON.stringify({ error: 'Kick token refresh failed', details: err?.message })
      };
    }
  }

  if (path === '/kick/webhook') {
    const method = event.requestContext?.http?.method ?? event.requestContext?.httpMethod ?? 'POST';
    if (method !== 'POST') {
      return { statusCode: 405, body: 'Method Not Allowed' };
    }

    const rawBody = getRawBody(event);
    if (!rawBody) {
      return { statusCode: 400, body: 'Empty body' };
    }

    if (!verifyKickSignature(event.headers ?? {}, rawBody)) {
      return { statusCode: 401, body: 'Invalid signature' };
    }

    let payload;
    try {
      payload = JSON.parse(rawBody);
    } catch (err) {
      console.error('Kick webhook invalid JSON', err);
      return { statusCode: 400, body: 'Invalid JSON' };
    }

    if (payload.challenge) {
      console.log('Kick webhook challenge received');
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ challenge: payload.challenge })
      };
    }

    console.log('Kick webhook event', JSON.stringify(payload));
    handleKickWebhookEvent(payload);

    return {
      statusCode: 200,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ok: true })
    };
  }

  /* ─────────────────────────────────────────────
   * 6)  /auth/twitch/refresh  → Refresh Twitch token
   * ────────────────────────────────────────────*/
  if (path === '/auth/twitch/refresh') {
    const refresh_token = query.refresh_token || getFromBody(event, 'refresh_token');
    if (!refresh_token) {
      return { statusCode: 400, body: 'Missing refresh_token' };
    }
    try {
      const tokenRes = await axios.post(
        TWITCH_TOKEN_URL,
        new URLSearchParams({
          grant_type: 'refresh_token',
          refresh_token,
          client_id: process.env.TWITCH_CLIENT_ID,
          client_secret: process.env.TWITCH_CLIENT_SECRET
        }),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );
      return {
        statusCode: 200,
        body: JSON.stringify(tokenRes.data)
      };
    } catch (err) {
      return {
        statusCode: 500,
        body: JSON.stringify({ error: 'Twitch token refresh failed', details: err?.message })
      };
    }
  }

  /* ─────────────────────────────────────────────
   * 404 dla pozostałych ścieżek
   * ────────────────────────────────────────────*/
  return { statusCode: 404, body: 'Not Found' };
};

/* Pomocnicza funkcja do wyciągania z body (POST/JSON) */
function getFromBody(event, key) {
  if (!event.body) return undefined;
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    return body[key];
  } catch {
    return undefined;
  }
}

function getRawBody(event) {
  if (!event.body) return '';
  if (event.isBase64Encoded) {
    return Buffer.from(event.body, 'base64').toString('utf8');
  }
  return event.body;
}

function parseBody(event) {
  if (!event.body) return null;
  try {
    const raw = getRawBody(event);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function verifyKickSignature(headers, rawBody) {
  const secret = process.env.KICK_WEBHOOK_SECRET;
  if (!secret) {
    console.warn('KICK_WEBHOOK_SECRET is not set – skipping signature verification');
    return true;
  }
  const signatureHeader =
    headers['kick-signature'] ||
    headers['kick-signature-hmac-sha256'] ||
    headers['x-kick-signature'] ||
    headers['kick-signature-hmac-sha256'.toLowerCase()];
  const timestamp =
    headers['kick-signature-timestamp'] ||
    headers['x-kick-timestamp'] ||
    headers['kick-timestamp'];

  if (!signatureHeader || !timestamp) {
    console.error('Kick webhook missing signature headers');
    return false;
  }

  const computed = crypto
    .createHmac('sha256', secret)
    .update(`${timestamp}.${rawBody}`)
    .digest('hex');

  const expected = signatureHeader.replace(/^sha256=/, '');
  const isValid =
    expected.length === computed.length &&
    crypto.timingSafeEqual(Buffer.from(expected, 'hex'), Buffer.from(computed, 'hex'));

  if (!isValid) {
    console.error('Kick webhook signature mismatch');
  }

  return isValid;
}

const MAX_MESSAGES_PER_CHANNEL = 200;
const kickChatMessages = new Map();

function handleKickWebhookEvent(payload) {
  const type = payload?.type;
  if (type === 'chat.message.sent') {
    const broadcasterId = payload.broadcaster_user_id ?? payload.channel?.id;
    if (!broadcasterId) {
      console.warn('chat.message.sent without broadcaster_user_id');
      return;
    }
    const normalized = normalizeChatMessage(payload);
    appendChatMessage(String(broadcasterId), normalized);
  }
}

function normalizeChatMessage(event) {
  return {
    id: event.id,
    broadcaster_user_id: event.broadcaster_user_id,
    chatroom_id: event.chatroom_id ?? event.metadata?.chatroom_id,
    content: event.content,
    created_at: event.created_at ?? new Date().toISOString(),
    sender: {
      id: event.sender?.id,
      username: event.sender?.username,
      display_name: event.sender?.display_name ?? event.sender?.slug ?? event.sender?.username,
      color: event.sender?.identity?.color
    },
    metadata: event.metadata ?? {}
  };
}

function appendChatMessage(broadcasterKey, message) {
  const list = kickChatMessages.get(broadcasterKey) ?? [];
  list.push(message);
  if (list.length > MAX_MESSAGES_PER_CHANNEL) {
    list.splice(0, list.length - MAX_MESSAGES_PER_CHANNEL);
  }
  kickChatMessages.set(broadcasterKey, list);
}
