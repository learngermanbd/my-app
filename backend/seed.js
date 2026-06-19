const { DatabaseSync } = require('node:sqlite');
const path = require('path');

const dbPath = path.join(__dirname, 'sportzfy.db');

function createDb() {
  return new DatabaseSync(dbPath);
}

function initSchema(db) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS categories (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      slug TEXT UNIQUE NOT NULL,
      icon_url TEXT,
      sort_order INTEGER DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS channels (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      category_id INTEGER NOT NULL,
      name TEXT NOT NULL,
      logo TEXT,
      is_live INTEGER DEFAULT 0,
      featured INTEGER DEFAULT 0,
      FOREIGN KEY (category_id) REFERENCES categories(id)
    );

    CREATE TABLE IF NOT EXISTS stream_links (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      channel_id INTEGER NOT NULL,
      label TEXT NOT NULL,
      url TEXT NOT NULL,
      quality TEXT DEFAULT 'Auto',
      is_active INTEGER DEFAULT 1,
      is_healthy INTEGER DEFAULT 1,
      last_checked TEXT,
      FOREIGN KEY (channel_id) REFERENCES channels(id)
    );

    CREATE TABLE IF NOT EXISTS analytics_events (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      event_name TEXT NOT NULL,
      params TEXT,
      device_id TEXT,
      created_at TEXT DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS app_config (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS live_events (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      channel_id INTEGER,
      starts_at TEXT,
      ends_at TEXT,
      is_live INTEGER DEFAULT 0,
      FOREIGN KEY (channel_id) REFERENCES channels(id)
    );

    CREATE TABLE IF NOT EXISTS m3u_schedules (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      url TEXT NOT NULL,
      target_category_id INTEGER,
      interval_hours INTEGER NOT NULL DEFAULT 24,
      last_run_at TEXT,
      last_result TEXT,
      is_active INTEGER DEFAULT 1,
      created_at TEXT DEFAULT (datetime('now')),
      FOREIGN KEY (target_category_id) REFERENCES categories(id)
    );

    CREATE TABLE IF NOT EXISTS stream_sessions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      session_id TEXT NOT NULL UNIQUE,
      device_id TEXT,
      device_type TEXT,
      channel_id INTEGER,
      channel_name TEXT,
      ip_address TEXT,
      user_agent TEXT,
      country TEXT,
      city TEXT,
      started_at TEXT DEFAULT (datetime('now')),
      last_heartbeat_at TEXT DEFAULT (datetime('now')),
      ended_at TEXT,
      duration_seconds INTEGER DEFAULT 0,
      is_active INTEGER DEFAULT 1,
      FOREIGN KEY (channel_id) REFERENCES channels(id)
    );

    CREATE INDEX IF NOT EXISTS idx_sessions_active ON stream_sessions(is_active);
    CREATE INDEX IF NOT EXISTS idx_sessions_channel ON stream_sessions(channel_id);
    CREATE INDEX IF NOT EXISTS idx_sessions_device ON stream_sessions(device_id);
    CREATE INDEX IF NOT EXISTS idx_sessions_heartbeat ON stream_sessions(last_heartbeat_at);
  `);
}

const TEST_STREAMS = [
  { label: 'Mux 720p', url: 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', quality: '720p' },
  { label: 'Tears of Steel', url: 'https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8', quality: '1080p' },
  { label: 'Apple BipBop', url: 'https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8', quality: '720p' },
  { label: 'Akamai Live', url: 'https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8', quality: '720p' }
];

const categories = [
  { name: 'Cricket', slug: 'cricket', icon_url: '🏏', sort_order: 1 },
  { name: 'Football', slug: 'football', icon_url: '⚽', sort_order: 2 },
  { name: 'Live TV', slug: 'live-tv', icon_url: '📺', sort_order: 3 }
];

const channels = [
  { category: 'cricket', name: 'Star Sports 1', logo: '', is_live: 1, featured: 1 },
  { category: 'cricket', name: 'Star Sports 2', logo: '', is_live: 0, featured: 0 },
  { category: 'cricket', name: 'Willow TV', logo: '', is_live: 1, featured: 1 },
  { category: 'football', name: 'Sky Sports', logo: '', is_live: 1, featured: 1 },
  { category: 'football', name: 'beIN Sports', logo: '', is_live: 0, featured: 0 },
  { category: 'football', name: 'ESPN', logo: '', is_live: 1, featured: 0 },
  { category: 'live-tv', name: 'News 24', logo: '', is_live: 1, featured: 1 },
  { category: 'live-tv', name: 'Entertainment HD', logo: '', is_live: 0, featured: 0 },
  { category: 'live-tv', name: 'Documentary Plus', logo: '', is_live: 0, featured: 1 }
];

const config = {
  app_name: 'StreamApp',
  app_version: '1.0.0',
  min_version: '1.0.0',
  api_base: 'http://10.0.2.2:3000/api/v1',
  features: JSON.stringify({ pip: true, floating_player: true, tv_mode: true, analytics: true }),
  theme: JSON.stringify({ primary: '#00c897', secondary: '#7c5cfc', background: '#0a0a0f' }),
  security_enabled: 'true',
  security_expected_package: 'com.streamapp',
  security_expected_hash: '',
  security_block_message: 'This app has been modified. Please download the original StreamApp from the official source.',
  security_download_url: ''
};

function seed() {
  const db = createDb();
  initSchema(db);

  const existing = db.prepare('SELECT COUNT(*) as c FROM categories').get();
  if (existing.c > 0) {
    console.log('Database already seeded. Skipping.');
    return false;
  }

  console.log('Seeding database...');

  const insertCategory = db.prepare('INSERT OR IGNORE INTO categories (name, slug, icon_url, sort_order) VALUES (?, ?, ?, ?)');
  const getCategory = db.prepare('SELECT id FROM categories WHERE slug = ?');
  const insertChannel = db.prepare('INSERT INTO channels (category_id, name, logo, is_live, featured) VALUES (?, ?, ?, ?, ?)');
  const insertLink = db.prepare(`INSERT INTO stream_links (channel_id, label, url, quality, is_active, is_healthy, last_checked) VALUES (?, ?, ?, ?, 1, 1, datetime('now'))`);
  const insertConfig = db.prepare('INSERT OR REPLACE INTO app_config (key, value) VALUES (?, ?)');

  for (const cat of categories) {
    insertCategory.run(cat.name, cat.slug, cat.icon_url, cat.sort_order);
  }

  for (const ch of channels) {
    const cat = getCategory.get(ch.category);
    const result = insertChannel.run(cat.id, ch.name, ch.logo, ch.is_live, ch.featured);
    const channelId = result.lastInsertRowid;
    for (const stream of TEST_STREAMS) {
      insertLink.run(channelId, stream.label, stream.url, stream.quality);
    }
  }

  for (const [key, value] of Object.entries(config)) {
    insertConfig.run(key, value);
  }

  db.prepare(`INSERT INTO live_events (title, channel_id, starts_at, is_live) VALUES (?, ?, datetime('now'), 1)`)
    .run('Live Cricket Match', 1);

  console.log('✅ Seed complete!');
  console.log(`   Categories: ${categories.length}`);
  console.log(`   Channels: ${channels.length}`);
  console.log(`   Stream links: ${channels.length * TEST_STREAMS.length}`);
  return true;
}

module.exports = { seed, createDb, initSchema, dbPath };

if (require.main === module) {
  seed();
}
