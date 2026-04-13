'use client'

import { useAuth } from "@/hooks/useAuth";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { authService } from "@/services/authService";

export const dynamic = 'force-dynamic'

export default function Home() {
  const { isAuthenticated, loading, user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    console.log('[Home] Auth state:', { isAuthenticated, loading, user: user?.id, role: user?.role })

    if (loading) {
      console.log('[Home] Still loading, waiting...')
      return;
    }

    // If not authenticated in context, but we have a token stored, wait for context to update
    if (!isAuthenticated && authService.getAccessToken()) {
      console.log('[Home] Token exists but context not updated yet, waiting...')
      return;
    }

    if (!isAuthenticated) {
      console.log('[Home] Not authenticated, redirecting to login')
      router.push("/login");
      return;
    }

    // Redirect based on role
    console.log('[Home] User role:', user?.role)
    switch (user?.role) {
      case "GUARDIAN":
        console.log('[Home] Redirecting to guardian/activities')
        router.push("/guardian/activities");
        break;
      case "TEACHER":
        console.log('[Home] Redirecting to teacher/attendance')
        router.push("/teacher/attendance");
        break;
      case "ADMIN":
      case "SUPERADMIN":
        console.log('[Home] Redirecting to admin/activities')
        router.push("/admin/activities");
        break;
      default:
        console.log('[Home] Unknown role, redirecting to login')
        router.push("/login");
    }
  }, [isAuthenticated, loading, user?.role, router]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <p>Redirigiendo...</p>
    </div>
  );
}
