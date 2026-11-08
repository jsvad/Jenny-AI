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
    if (!env.AI) return respond({ error: 'Workers AI binding is missing' }, 503, headers);

    try {
      const body = await request.json();
      const message = typeof body.message === 'string' ? body.message.trim().slice(0, 2000) : '';
      const history = typeof body.conversation === 'string' ? body.conversation.slice(-5000) : '';
      if (!message) return respond({ error: 'Message required' }, 400, headers);

      const messages = [
        {
          role: 'system',
          content: 'تو جنی هستی؛ دستیار فارسی‌زبان، دوستانه، دقیق و محترم. به فارسی روان پاسخ بده مگر کاربر زبان دیگری بخواهد. اگر چیزی را نمی‌دانی صادقانه بگو و ادعای دسترسی یا انجام کاری که نداری نکن. پاسخ‌ها را متناسب با سن کاربر نگه دار.'
        }
      ];

      if (history) {
        messages.push({ role: 'user', content: 'خلاصه گفت‌وگوی قبلی:\n' + history });
      }
      messages.push({ role: 'user', content: message });

      const result = await env.AI.run('@cf/google/gemma-4-26b-a4b-it', {
        messages,
        chat_template_kwargs: { enable_thinking: false },
        max_tokens: 700
      });

      const reply =
        (typeof result.response === 'string' && result.response) ||
        (result.choices && result.choices[0] && result.choices[0].message && result.choices[0].message.content) ||
        '';

      if (!reply.trim()) return respond({ error: 'Empty AI response' }, 502, headers);
      return respond({ reply: reply.trim() }, 200, headers);
    } catch (error) {
      return respond({ error: 'AI request failed' }, 502, headers);
    }
  }
};

function respond(value, status, headers) {
  return new Response(JSON.stringify(value), { status, headers });
}
