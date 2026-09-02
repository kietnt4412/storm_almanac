/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      // Kept in sync with plan.html so the built product and the plan that
      // describes it do not drift into looking like two different projects.
      colors: {
        ground: '#EFF2F1',
        surface: '#FBFCFC',
        ink: '#15211F',
        accent: '#0D6F68',
        signal: '#9C6414',
      },
    },
  },
  plugins: [],
};
