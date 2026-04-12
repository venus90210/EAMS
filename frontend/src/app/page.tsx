'use client'

import { useAuth } from "@/hooks/useAuth";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function Home() {
  const { isAuthenticated, loading, user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated) {
      router.push("/login");
      return;
    }

    // Redirect based on role
    switch (user?.role) {
      case "GUARDIAN":
        router.push("/guardian/activities");
        break;
      case "TEACHER":
        router.push("/teacher/attendance");
        break;
      case "ADMIN":
      case "SUPERADMIN":
        router.push("/admin/activities");
        break;
      default:
        router.push("/login");
    }
  }, [isAuthenticated, loading, user?.role, router]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <p>Redirigiendo...</p>
    </div>
  );
}
