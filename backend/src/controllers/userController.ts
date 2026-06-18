import { Request, Response } from "express";

const users = [
  { id: 1, name: "Alice Johnson", email: "alice@example.com" },
  { id: 2, name: "Bob Smith", email: "bob@example.com" },
  { id: 3, name: "Charlie Brown", email: "charlie@example.com" },
];

export function getUsers(req: Request, res: Response): void {
  const page = Math.max(1, parseInt(req.query.page as string, 10) || 1);
  const limit = Math.min(100, Math.max(1, parseInt(req.query.limit as string, 10) || 10));
  const start = (page - 1) * limit;
  const paginatedUsers = users.slice(start, start + limit);
  res.json({ users: paginatedUsers, page, limit, total: users.length });
}

export function getUserById(req: Request, res: Response): void {
  const id = parseInt(req.params.id, 10);
  if (isNaN(id) || id < 1) {
    res.status(400).json({ error: { message: "Invalid user ID" } });
    return;
  }
  const user = users.find((u) => u.id === id);
  if (!user) {
    res.status(404).json({ error: { message: "User not found" } });
    return;
  }
  res.json({ user });
}
