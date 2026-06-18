function requireEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    console.error(`FATAL: ${key} environment variable is not set`);
    process.exit(1);
  }
  return value;
}

export const JWT_SECRET = requireEnv("JWT_SECRET");
export const ADMIN_PASSWORD = requireEnv("ADMIN_PASSWORD");
export const TURSO_DATABASE_URL = requireEnv("TURSO_DATABASE_URL");
export const TURSO_AUTH_TOKEN = requireEnv("TURSO_AUTH_TOKEN");
