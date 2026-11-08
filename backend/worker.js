export default {
  async fetch(request, env) {
    const headers = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
      'Content-Type': 'application/json; charset=utf-8'
    };
    if (request.method === 'OPTIONS') return new Response(null, { headers });
    if (request.method !== 'POST') return respond({ error: 'Method not allowed' }, 405, headers);
    if (!env.OPENAI_API_KEY) return respond({ error: 'Backend not configured' }, 503, headers);
    try {
      const body = await request.json();
      const message = typeof body.message === 'string' ? body.message.trim().slice(0, 2000) : '';
      const history = typeof body.conversation === 'string' ? body.conversation.slice(-5000) : '';
      if (!message) return respond({ error: 'Message required' }, 400, headers);
      const upstream = await fetch('https://api.openai.com/v1/responses', {
        method: 'POST',
        headers: { Authorization: 'Bearer ' + env.OPENAI_API_KEY, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: env.OPENAI_MODEL || 'gpt-6-astra',
          instructions: 'تو جنی هستی؛ دستیار فارسی‌زبان، دوستانه، دقیق و محترم. به فارسی روان پاسخ بده مگر کاربر زبان دیگری بخواهد. اگر چیزی را نمی‌دانی صادقانه بگو و ادعای دسترسی یا انجام کاری که نداری نکن. پاسخ‌ها را متناسب با سن کاربر نگه دار.',
          input: (history ? 'گفت‌وگوی قبلی:\n' + history + '\n\n' : '') + 'پیام تازه کاربر: ' + message,
          max_output_tokens: 700
        })
      });
      const data = await upstream.json();
      if (!upstream.ok) return respond({ error: 'AI provider error', status: upstream.status }, 502, headers);
      const reply = (data.output || []).flatMap(item => item.content || []).filter(part => part.type === 'output_text').map(part => part.text || '').join('\n').trim();
      if (!reply) return respond({ error: 'Empty AI response' }, 502, headers);
      return respond({ reply }, 200, headers);
    } catch (_) { return respond({ error: 'Invalid request' }, 400, headers); }
  }
};
function respond(value, status, headers) { return new Response(JSON.stringify(value), { status, headers }); }