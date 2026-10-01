import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createBrowserRouter,
  createRoutesFromElements,
  Navigate,
  Route,
  RouterProvider,
  ScrollRestoration,
} from 'react-router-dom';
import { App } from './App';
import { Home } from './routes/Home';
import { Inventory } from './routes/Inventory';
import { Goals } from './routes/Goals';
import { Roster } from './routes/Roster';
import { PlanView } from './routes/PlanView';
import { Pulls } from './routes/Pulls';
import { Catalog } from './routes/Catalog';
import { EntityPage } from './routes/EntityPage';
import './index.css';
import './motion.css';
import './looks.css';
import './faces.css';
import { applyGame, storedGame } from './ui/gameChoice';
import { waitForEntity } from './catalog/prefetch';

// Before the first render, so a reader who chose a game never sees another
// game's colours flash first. The shell keeps it in step from then on.
applyGame(storedGame());

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

// A data router rather than <BrowserRouter> (C2.19), for one thing: a link
// marked `viewTransition` morphs the page it leaves into the page it opens — a
// character's emblem flies into the dossier, the step bar's segment slides.
// The route tree is the one it always was; tests render pieces of it under
// MemoryRouter, where those links are plain links.
//
// The one loader waits a moment for a character before opening their page, so
// the emblem has a dossier to land in. Never on the first load: a reader who
// follows a link in has nothing to fly from, and would watch a blank page wait.
let router: ReturnType<typeof createBrowserRouter> | undefined;
const initialised = () => router?.state.initialized ?? false;

// A page opened from a link starts at its top, and Back returns to where the
// reader was. Before the data router a page opened scrolled as far down as the
// one left, which would fly an emblem to a dossier above the screen. Here and
// not in App, which tests render under MemoryRouter, where this cannot run.
function Root() {
  return (
    <>
      <ScrollRestoration />
      <App />
    </>
  );
}

router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<Root />}>
      <Route index element={<Home />} />
      <Route path="inventory" element={<Inventory />} />
      <Route path="roster" element={<Roster />} />
      <Route path="goals" element={<Goals />} />
      <Route path="plan" element={<PlanView />} />
      <Route path="pulls" element={<Pulls />} />
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
      <Route
        path="catalog/:game/:entity"
        element={<EntityPage />}
        loader={({ params }) =>
          initialised() ? waitForEntity(queryClient, params.game ?? '', params.entity ?? '') : null
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Route>,
  ),
);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
);
