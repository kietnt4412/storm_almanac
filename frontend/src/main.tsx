import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { App } from './App';
import { Home } from './routes/Home';
import { Inventory } from './routes/Inventory';
import { Goals } from './routes/Goals';
import { Roster } from './routes/Roster';
import { PlanView } from './routes/PlanView';
import { Catalog } from './routes/Catalog';
import { EntityPage } from './routes/EntityPage';
import './index.css';

// Server state goes in TanStack Query; planner-local state (a half-edited
// inventory, an unsaved goal set) goes in Zustand. Keeping the two apart is
// what makes offline editing tractable.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
      // A reader who comes back to a tab after an hour on a train wants what
      // the server has now, and the refetch is the moment the outbox has
      // usually just flushed into.
      refetchOnWindowFocus: true,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />}>
            <Route index element={<Home />} />
            <Route path="inventory" element={<Inventory />} />
            <Route path="roster" element={<Roster />} />
            <Route path="goals" element={<Goals />} />
            <Route path="plan" element={<PlanView />} />
            {/*
              The game is in the path for the catalog and nowhere else. A catalog
              page is the one thing here a stranger can be sent a link to, and a
              link that only works for whoever has the right profile selected is
              not a link. Everything under /api/me takes the profile from the
              store instead, because it is the reader's own state and not a
              coordinate in the game.
            */}
            <Route path="catalog" element={<Catalog />} />
            <Route path="catalog/:game" element={<Catalog />} />
            <Route path="catalog/:game/:entity" element={<EntityPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
