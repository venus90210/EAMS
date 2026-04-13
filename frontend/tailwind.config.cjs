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
        primary: '#6C4AE2',
        accent: '#00A0A8',
        muted: '#6B7280',
        surface: '#FFFFFF',
        bg: '#F6F7FB'
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui']
      },
      borderRadius: {
        lg: '12px'
      },
      boxShadow: {
        card: '0 8px 24px rgba(27,31,35,0.08)'
      }
    }
  },
  plugins: []
};
