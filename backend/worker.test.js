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
