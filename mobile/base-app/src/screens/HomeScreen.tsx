import React, { useEffect, useState } from "react";
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator } from "react-native";
import { api } from "../services/api";

export function HomeScreen() {
  const [status, setStatus] = useState("Checking...");
  const [loading, setLoading] = useState(true);

  const checkBackend = async () => {
    setLoading(true);
    const { data, error } = await api.healthCheck();
    if (error) { setStatus("Offline: " + error); }
    else if (data) { setStatus("Online - " + data.status); }
    setLoading(false);
  };

  useEffect(() => { checkBackend(); }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>My App</Text>
      <Text style={styles.subtitle}>User Portal</Text>
      <View style={styles.card}>
        <Text style={styles.cardTitle}>Backend Status</Text>
        {loading ? <ActivityIndicator size="small" color="#4A90D9" /> :
          <Text style={[styles.status, { color: status.startsWith("Online") ? "#27AE60" : "#E74C3C" }]}>{status}</Text>}
        <TouchableOpacity style={styles.button} onPress={checkBackend}>
          <Text style={styles.buttonText}>Refresh</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#F5F7FA", alignItems: "center", justifyContent: "center", padding: 20 },
  title: { fontSize: 32, fontWeight: "bold", color: "#2C3E50", marginBottom: 4 },
  subtitle: { fontSize: 16, color: "#7F8C8D", marginBottom: 40 },
  card: { backgroundColor: "#FFFFFF", borderRadius: 12, padding: 24, width: "100%", alignItems: "center", elevation: 4 },
  cardTitle: { fontSize: 18, fontWeight: "600", color: "#34495E", marginBottom: 12 },
  status: { fontSize: 16, fontWeight: "500", marginBottom: 16 },
  button: { backgroundColor: "#4A90D9", paddingVertical: 10, paddingHorizontal: 32, borderRadius: 8 },
  buttonText: { color: "#FFFFFF", fontSize: 14, fontWeight: "600" },
});
