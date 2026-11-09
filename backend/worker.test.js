import test from 'node:test';
import assert from 'node:assert/strict';
import worker from './worker.js';

const makeRequest = (body, method = 'POST') => new Request('https://jenny.test/chat', {
  method,
  headers: { 'Content-Type': 'application/json' },
  ...(body === undefined ? {} : { body: JSON.stringify(body) })
});

test('OPTIONS preflight succeeds without an AI binding', async () => {
  const response = await worker.fetch(makeRequest(undefined, 'OPTIONS'), {});
  assert.equal(response.status, 200);
  assert.equal(response.headers.get('Access-Control-Allow-Origin'), '*');
});

test('non-POST methods are rejected', async () => {
  const response = await worker.fetch(makeRequest(undefined, 'GET'), {});
  assert.equal(response.status, 405);
  assert.deepEqual(await response.json(), { error: 'Method not allowed' });
});

test('missing Workers AI binding returns service unavailable', async () => {
  const response = await worker.fetch(makeRequest({ message: 'سلام' }), {});
  assert.equal(response.status, 503);
  assert.deepEqual(await response.json(), { error: 'Workers AI binding is missing' });
});

test('empty messages are rejected before invoking the model', async () => {
  let called = false;
  const response = await worker.fetch(makeRequest({ message: '   ' }), {
    AI: { run: async () => { called = true; return { response: 'unexpected' }; } }
  });
  assert.equal(response.status, 400);
  assert.equal(called, false);
});

test('valid messages return the model reply and pass conversation context', async () => {
  let captured;
  const response = await worker.fetch(makeRequest({
    message: 'سلام جنی',
    conversation: 'پیام قبلی'
  }), {
    AI: {
      run: async (model, input) => {
        captured = { model, input };
        return { response: 'سلام! خوش اومدی.' };
      }
    }
  });

  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), { reply: 'سلام! خوش اومدی.' });
  assert.equal(captured.model, '@cf/google/gemma-4-26b-a4b-it');
  assert.ok(captured.input.messages.some(item => item.content.includes('پیام قبلی')));
  assert.equal(captured.input.messages.at(-1).content, 'سلام جنی');
});

test('model failures return a controlled gateway error', async () => {
  const response = await worker.fetch(makeRequest({ message: 'سلام' }), {
    AI: { run: async () => { throw new Error('simulated provider failure'); } }
  });
  assert.equal(response.status, 502);
  assert.deepEqual(await response.json(), { error: 'AI request failed' });
});


const videoRequest = (path, body, method = 'POST') => new Request(`https://jenny.test${path}`, {
  method,
  headers: { 'Content-Type': 'application/json' },
  ...(body === undefined ? {} : { body: JSON.stringify(body) })
});

test('Fal video route requires a server-side API key', async () => {
  const response = await worker.fetch(videoRequest('/video/generate', { prompt: 'A calm ocean' }), {});
  assert.equal(response.status, 503);
  assert.match((await response.json()).error, /not configured/);
});

test('Fal video route validates prompt and bounded duration before provider call', async () => {
  const response = await worker.fetch(videoRequest('/video/generate', {
    prompt: 'A calm ocean', duration: 30
  }), { FAL_KEY: 'test-key' });
  assert.equal(response.status, 400);
});

test('Fal video route submits a safe text-to-video request to the fixed endpoint', async () => {
  const originalFetch = globalThis.fetch;
  let captured;
  globalThis.fetch = async (url, options) => {
    captured = { url, options, body: JSON.parse(options.body) };
    return new Response(JSON.stringify({ request_id: '123e4567-e89b-12d3-a456-426614174000', status: 'IN_QUEUE' }), { status: 200 });
  };
  try {
    const response = await worker.fetch(videoRequest('/video/generate', {
      prompt: 'A calm ocean at sunset', duration: 5
    }), { FAL_KEY: 'test-key' });
    assert.equal(response.status, 202);
    assert.equal(captured.url, 'https://queue.fal.run/minimax/h3-max-turbo/image-to-video');
    assert.equal(captured.options.headers.Authorization, 'Key test-key');
    assert.equal(captured.body.enable_safety_checker, true);
    assert.equal(captured.body.sync_mode, false);
    assert.equal(captured.body.prompt, 'A calm ocean at sunset');
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test('Fal status route returns a completed MP4 URL from the provider', async () => {
  const originalFetch = globalThis.fetch;
  let calls = 0;
  globalThis.fetch = async () => {
    calls += 1;
    if (calls === 1) return new Response(JSON.stringify({ status: 'COMPLETED' }), { status: 200 });
    return new Response(JSON.stringify({ video: { url: 'https://cdn.fal.media/test.mp4', content_type: 'video/mp4' } }), { status: 200 });
  };
  try {
    const request = videoRequest('/video/status?request_id=123e4567-e89b-12d3-a456-426614174000', undefined, 'GET');
    const response = await worker.fetch(request, { FAL_KEY: 'test-key' });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), {
      request_id: '123e4567-e89b-12d3-a456-426614174000',
      status: 'COMPLETED',
      video_url: 'https://cdn.fal.media/test.mp4',
      content_type: 'video/mp4'
    });
    assert.equal(calls, 2);
  } finally {
    globalThis.fetch = originalFetch;
  }
});
