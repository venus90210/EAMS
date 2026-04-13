import React from 'react';

export default function Hero({
  title = 'Bienvenido a EAMS',
  subtitle = 'Gestiona actividades, inscripciones y asistencia de forma sencilla',
  cta = 'Comenzar'
}: {
  title?: string;
  subtitle?: string;
  cta?: string;
}) {
  return (
    <section className="py-12">
      <div className="container mx-auto px-6 lg:px-12 flex flex-col lg:flex-row items-center gap-8">
        <div className="flex-1">
          <div className="card p-8">
            <h1 className="text-3xl lg:text-4xl font-bold mb-4" style={{ color: 'var(--text)' }}>{title}</h1>
            <p className="text-muted mb-6" style={{ color: 'var(--muted)' }}>{subtitle}</p>
            <div className="flex items-center gap-4">
              <button className="btn-primary hero-cta inline-flex items-center gap-2">
                {cta}
              </button>
              <a className="text-sm text-muted hover:underline" href="#">Ver demo</a>
            </div>
          </div>
        </div>
        <div className="flex-1 flex justify-center lg:justify-end">
          <div className="w-full max-w-md">
            <img src="/hero-illustration.png" alt="Hero" className="rounded-lg shadow-card" />
          </div>
        </div>
      </div>
    </section>
  );
}
