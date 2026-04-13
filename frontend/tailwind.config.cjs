/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{js,ts,jsx,tsx}',
    './pages/**/*.{js,ts,jsx,tsx}',
    './components/**/*.{js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        primary: '#0A66C2',
        accent: '#0A8A2E',
        muted: '#65676B',
        surface: '#FFFFFF',
        bg: '#F3F6F8',
        border: '#E0E0E0'
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Helvetica Neue', 'sans-serif']
      },
      borderRadius: {
        lg: '8px',
        xl: '12px',
        button: '24px'
      },
      boxShadow: {
        card: '0 2px 4px rgba(0,0,0,0.1)',
        'card-hover': '0 4px 12px rgba(0,0,0,0.15)'
      }
    }
  },
  plugins: []
};
