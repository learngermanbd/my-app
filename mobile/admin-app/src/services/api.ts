import { API_URL } from "./config";
interface ApiResponse<T> { data?: T; error?: string; }
async function request<T>(endpoint: string, options?: RequestInit): Promise<ApiResponse<T>> {
  try {
    const res = await fetch(`${API_URL}${endpoint}`, { headers: { "Content-Type": "application/json" }, ...options });
    const data = await res.json();
    if (!res.ok) return { error: data.error?.message || "Request failed" };
    return { data };
  } catch (err) { return { error: err instanceof Error ? err.message : "Network error" }; }
}
export const api = {
  healthCheck: () => request<{status:string;timestamp:string}>("/health"),
  getStats: () => request<{totalUsers:number;activeSessions:number;serverUptime:number}>("/admin/stats"),
  login: (password: string) => request<{token:string}>("/admin/login", { method: "POST", body: JSON.stringify({password}) }),
};
