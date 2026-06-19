import { Request, Response, NextFunction } from "express";

export interface AppError extends Error {
  statusCode?: number;
}

const isProduction = process.env.NODE_ENV === "production";

export function errorHandler(
  err: AppError,
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  const statusCode = err.statusCode || 500;
  console.error(`[Error] ${statusCode} - ${err.message}`);
  res.status(statusCode).json({
    error: {
      message: isProduction ? "Internal Server Error" : err.message || "Internal Server Error",
      ...(!isProduction && { stack: err.stack }),
    },
  });
}
