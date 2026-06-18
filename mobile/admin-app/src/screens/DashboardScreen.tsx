import React, { useEffect, useState } from "react";
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView } from "react-native";
import { api } from "../services/api";

interface Stats { totalUsers: number; activeSessions: number; serverUptime: number; }

export function DashboardScreen() {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    setLoading(true); setError(null);
    const { data, error: err } = await api.getStats();
    if (err) setError(err); else if (data) setStats(data);
    setLoading(false);
  };

  useEffect(() => { fetchStats(); }, []);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Admin Dashboard</Text>
      <Text style={styles.subtitle}>System Overview</Text>
      {loading ? <ActivityIndicator size="large" color="#8E44AD" style={{marginTop:40}} /> :
       error ? <View style={styles.errorCard}><Text style={styles.errorText}>{error}</Text><TouchableOpacity style={styles.retryBtn} onPress={fetchStats}><Text style={styles.retryText}>Retry</Text></TouchableOpacity></View> :
       <View style={styles.grid}>
        <View style={[styles.statCard,{borderLeftColor:"#3498DB"}]}><Text style={styles.statValue}>{stats?.totalUsers??0}</Text><Text style={styles.statLabel}>Total Users</Text></View>
        <View style={[styles.statCard,{borderLeftColor:"#27AE60"}]}><Text style={styles.statValue}>{stats?.activeSessions??0}</Text><Text style={styles.statLabel}>Active Sessions</Text></View>
        <View style={[styles.statCard,{borderLeftColor:"#F39C12"}]}><Text style={styles.statValue}>{stats?Math.floor(stats.serverUptime/3600)+"h":"0h"}</Text><Text style={styles.statLabel}>Server Uptime</Text></View>
       </View>}
      <TouchableOpacity style={styles.refreshBtn} onPress={fetchStats}><Text style={styles.refreshText}>Refresh Data</Text></TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, backgroundColor: "#F5F7FA", alignItems: "center", padding: 20, paddingTop: 60 },
  title: { fontSize: 28, fontWeight: "bold", color: "#2C3E50" },
  subtitle: { fontSize: 14, color: "#7F8C8D", marginBottom: 32, marginTop: 4 },
  grid: { width: "100%", gap: 16 },
  statCard: { backgroundColor: "#FFFFFF", borderRadius: 10, padding: 20, borderLeftWidth: 4, elevation: 3 },
  statValue: { fontSize: 28, fontWeight: "bold", color: "#2C3E50" },
  statLabel: { fontSize: 13, color: "#95A5A6", marginTop: 4 },
  errorCard: { backgroundColor: "#FFF5F5", borderRadius: 10, padding: 24, width: "100%", alignItems: "center" },
  errorText: { color: "#E74C3C", fontSize: 14, textAlign: "center", marginBottom: 12 },
  retryBtn: { backgroundColor: "#E74C3C", paddingVertical: 8, paddingHorizontal: 24, borderRadius: 6 },
  retryText: { color: "#FFFFFF", fontWeight: "600", fontSize: 13 },
  refreshBtn: { marginTop: 24, backgroundColor: "#8E44AD", paddingVertical: 12, paddingHorizontal: 32, borderRadius: 8 },
  refreshText: { color: "#FFFFFF", fontSize: 14, fontWeight: "600" },
});
