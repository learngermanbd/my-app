const axios = require('axios');
const { DatabaseSync } = require('node:sqlite');
const path = require('path');

const db = new DatabaseSync(path.join(__dirname, 'sportzfy.db'));

async function checkAllStreams() {
  const links = db.prepare('SELECT id, url FROM stream_links WHERE is_active = 1').all();
  console.log(`🔍 Checking ${links.length} stream links...`);

  for (const link of links) {
    try {
      const response = await axios.get(link.url, {
        timeout: 10000,
        responseType: 'text',
        headers: { 'User-Agent': 'Mozilla/5.0' }
      });
      const isHealthy = response.data.includes('#EXTM3U');
      db.prepare(`UPDATE stream_links SET is_healthy = ?, last_checked = datetime('now') WHERE id = ?`)
        .run(isHealthy ? 1 : 0, link.id);
      if (!isHealthy) console.log(`  💀 Link ${link.id} is dead`);
    } catch (e) {
      db.prepare(`UPDATE stream_links SET is_healthy = 0, last_checked = datetime('now') WHERE id = ?`)
        .run(link.id);
      console.log(`  💀 Link ${link.id} error: ${e.message}`);
    }
  }
  console.log('✅ Check complete.');
}

checkAllStreams();
setInterval(checkAllStreams, 60_000);
