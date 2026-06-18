import { Router } from "express";
import { getUsers, getUserById } from "../controllers/userController";
export const usersRouter = Router();
usersRouter.get("/", getUsers);
usersRouter.get("/:id", getUserById);
