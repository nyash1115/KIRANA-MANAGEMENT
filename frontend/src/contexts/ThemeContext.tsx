import React, { createContext, useContext, useState, useMemo } from 'react';
import { ThemeProvider as MuiThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

interface ThemeContextType {
  mode: 'light' | 'dark';
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
  mode: 'light',
  toggleTheme: () => {},
});

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [mode, setMode] = useState<'light' | 'dark'>(() => {
    const saved = localStorage.getItem('kirana_theme');
    return (saved as 'light' | 'dark') || 'light';
  });

  const toggleTheme = () => {
    setMode((prev) => {
      const next = prev === 'light' ? 'dark' : 'light';
      localStorage.setItem('kirana_theme', next);
      return next;
    });
  };

  const theme = useMemo(() => {
    return createTheme({
      palette: {
        mode,
        primary: {
          main: '#10B981', // Crisp Emerald Green (popular for Indian grocery/fintech apps)
          light: '#34D399',
          dark: '#059669',
          contrastText: '#FFFFFF',
        },
        secondary: {
          main: '#F59E0B', // Warm Amber (accent)
          light: '#FBBF24',
          dark: '#D97706',
        },
        background: {
          default: mode === 'dark' ? '#0F172A' : '#F8FAFC',
          paper: mode === 'dark' ? '#1E293B' : '#FFFFFF',
        },
        text: {
          primary: mode === 'dark' ? '#F1F5F9' : '#0F172A',
          secondary: mode === 'dark' ? '#94A3B8' : '#64748B',
        },
        error: { main: '#EF4444' },
        warning: { main: '#F59E0B' },
        info: { main: '#3B82F6' },
        success: { main: '#10B981' },
      },
      typography: {
        fontFamily: '"Inter", "Segoe UI", Roboto, sans-serif',
        h5: { fontWeight: 700 },
        h6: { fontWeight: 600 },
        subtitle1: { fontWeight: 600 },
        button: { textTransform: 'none', fontWeight: 600 },
      },
      shape: {
        borderRadius: 10,
      },
      components: {
        MuiButton: {
          styleOverrides: {
            root: {
              boxShadow: 'none',
              '&:hover': {
                boxShadow: '0 4px 12px rgba(16, 185, 129, 0.2)',
              },
            },
          },
        },
        MuiPaper: {
          styleOverrides: {
            root: {
              backgroundImage: 'none',
            },
          },
        },
      },
    });
  }, [mode]);

  return (
    <ThemeContext.Provider value={{ mode, toggleTheme }}>
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </MuiThemeProvider>
    </ThemeContext.Provider>
  );
};

export const useAppTheme = () => useContext(ThemeContext);
