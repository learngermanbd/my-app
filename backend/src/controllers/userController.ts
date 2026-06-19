import { Request, Response, NextFunction } from "express";
import { getDb } from "../config/db";

export async function getUsers(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const db = getDb();
    const page = Math.max(1, parseInt(req.query.page as string, 10) || 1);
    const limit = Math.min(100, Math.max(1, parseInt(req.query.limit as string, 10) || 10));
    const offset = (page - 1) * limit;

    const countResult = await db.execute("SELECT COUNT(*) as count FROM users");
    const total = Number(countResult.rows[0]?.count ?? 0);

    const result = await db.execute({
      sql: "SELECT * FROM users ORDER BY id LIMIT ? OFFSET ?",
      args: [limit, offset],
    });

    const users = result.rows.map((row) => ({
      id: Number(row.id),
      name: String(row.name),
      email: String(row.email),
      created_at: String(row.created_at),
    }));

    res.json({ users, page, limit, total });
  } catch (err) {
    next(err);
  }
}

export async function getUserById(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id) || id < 1) {
      res.status(400).json({ error: { message: "Invalid user ID" } });
      return;
    }

    const db = getDb();
    const result = await db.execute({
      sql: "SELECT * FROM users WHERE id = ?",
      args: [id],
    });

    if (result.rows.length === 0) {
      res.status(404).json({ error: { message: "User not found" } });
      return;
    }

    const row = result.rows[0];
    res.json({
      user: {
        id: Number(row.id),
        name: String(row.name),
        email: String(row.email),
        created_at: String(row.created_at),
      },
    });
  } catch (err) {
    next(err);
  }
}

export async function createUser(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { name, email } = req.body;

    if (!name || typeof name !== "string" || !email || typeof email !== "string") {
      res.status(400).json({ error: { message: "Name and email are required" } });
      return;
    }

    const db = getDb();
    const result = await db.execute({
      sql: "INSERT INTO users (name, email) VALUES (?, ?) RETURNING *",
      args: [name, email],
    });

    const row = result.rows[0];
    res.status(201).json({
      user: {
        id: Number(row.id),
        name: String(row.name),
        email: String(row.email),
        created_at: String(row.created_at),
      },
    });
  } catch (err) {
    if (err instanceof Error && err.message.includes("UNIQUE constraint")) {
      res.status(409).json({ error: { message: "Email already exists" } });
      return;
    }
    next(err);
  }
}

export async function updateUser(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id) || id < 1) {
      res.status(400).json({ error: { message: "Invalid user ID" } });
      return;
    }

    const { name, email } = req.body;
    const db = getDb();

    const existing = await db.execute({ sql: "SELECT id FROM users WHERE id = ?", args: [id] });
    if (existing.rows.length === 0) {
      res.status(404).json({ error: { message: "User not found" } });
      return;
    }

    if (name !== undefined && typeof name !== "string") {
      res.status(400).json({ error: { message: "Name must be a string" } });
      return;
    }
    if (email !== undefined && typeof email !== "string") {
      res.status(400).json({ error: { message: "Email must be a string" } });
      return;
    }

    if (name || email) {
      const updates: string[] = [];
      const args: (string | number)[] = [];
      if (name) { updates.push("name = ?"); args.push(name); }
      if (email) { updates.push("email = ?"); args.push(email); }
      args.push(id);

      const result = await db.execute({
        sql: `UPDATE users SET ${updates.join(", ")} WHERE id = ? RETURNING *`,
        args,
      });

      const row = result.rows[0];
      res.json({
        user: {
          id: Number(row.id),
          name: String(row.name),
          email: String(row.email),
          created_at: String(row.created_at),
        },
      });
    } else {
      res.status(400).json({ error: { message: "At least one field (name or email) is required" } });
    }
  } catch (err) {
    if (err instanceof Error && err.message.includes("UNIQUE constraint")) {
      res.status(409).json({ error: { message: "Email already exists" } });
      return;
    }
    next(err);
  }
}

export async function deleteUser(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const id = parseInt(req.params.id, 10);
    if (isNaN(id) || id < 1) {
      res.status(400).json({ error: { message: "Invalid user ID" } });
      return;
    }

    const db = getDb();
    const result = await db.execute({ sql: "DELETE FROM users WHERE id = ? RETURNING *", args: [id] });

    if (result.rows.length === 0) {
      res.status(404).json({ error: { message: "User not found" } });
      return;
    }

    res.json({ message: "User deleted" });
  } catch (err) {
    next(err);
  }
}
