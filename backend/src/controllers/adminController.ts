import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import { JWT_SECRET, ADMIN_PASSWORD } from "../config/env";
import { getDb } from "../config/db";

export async function login(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { password } = req.body;

    if (!password || typeof password !== "string") {
      res.status(400).json({ error: { message: "Password is required" } });
      return;
    }

    if (password.length > 128) {
      res.status(400).json({ error: { message: "Password too long" } });
      return;
    }

    if (password !== ADMIN_PASSWORD) {
      res.status(401).json({ error: { message: "Invalid credentials" } });
      return;
    }

    const token = jwt.sign({ adminId: "admin" }, JWT_SECRET, { expiresIn: "8h" });
    res.json({ token });
  } catch (err) {
    next(err);
  }
}

export async function getStats(
  _req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const db = getDb();
    const result = await db.execute("SELECT COUNT(*) as count FROM users");
    const totalUsers = Number(result.rows[0]?.count ?? 0);

    res.json({ totalUsers, serverUptime: process.uptime() });
  } catch (err) {
    next(err);
  }
}
