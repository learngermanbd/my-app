import { Router } from "express";
import rateLimit from "express-rate-limit";
import { login, getStats } from "../controllers/adminController";
import { authMiddleware } from "../middleware/auth";

export const adminRouter = Router();
const loginLimiter = rateLimit({ windowMs: 15 * 60 * 1000, max: 10, message: { error: { message: "Too many login attempts" } }, standardHeaders: true, legacyHeaders: false });
adminRouter.post("/login", loginLimiter, login);
adminRouter.get("/stats", authMiddleware, getStats);
