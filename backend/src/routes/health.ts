import { Router, Request, Response, NextFunction } from "express";

export const healthRouter = Router();

healthRouter.get("/", (_req: Request, res: Response, _next: NextFunction) => {
  res.json({ status: "ok", timestamp: new Date().toISOString(), uptime: process.uptime() });
});
