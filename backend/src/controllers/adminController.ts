import { Request, Response } from "express";
import jwt from "jsonwebtoken";
import { JWT_SECRET, ADMIN_PASSWORD } from "../config/env";

export function login(req: Request, res: Response): void {
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
}

export function getStats(_req: Request, res: Response): void {
  res.json({ totalUsers: 3, activeSessions: 2, serverUptime: process.uptime() });
}
