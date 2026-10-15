/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          950: '#0b0f14',
          900: '#10161d',
          800: '#161e27',
          700: '#1e2834',
          600: '#2a3645',
          500: '#3c4a5c',
          400: '#5a6b80',
          300: '#8595a8',
          200: '#b4c0cc',
          100: '#dde3e9',
          50: '#f3f5f7',
        },
        brand: {
          700: '#0d5f4f',
          600: '#0f7a63',
          500: '#13977a',
          400: '#2fb595',
          300: '#6fd1b8',
        },
        amber: {
          500: '#d9962c',
        },
        rust: {
          500: '#c24a3a',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
      boxShadow: {
        panel: '0 1px 2px rgba(10, 14, 18, 0.4), 0 1px 0 rgba(255,255,255,0.02) inset',
      },
    },
  },
  plugins: [],
}
