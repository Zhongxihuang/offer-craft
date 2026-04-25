# AI Career Decision & Interview Prep Agent Frontend

This frontend is the lightweight UI shell for the workflow-first career agent backend.

## Current Product Role

- Present the app as a career decision and interview prep agent
- Make the structured workflow the default homepage
- Provide support-chat access as a secondary follow-up mode
- Restore the latest workflow result from local storage when possible
- Support one-click `中文 | EN` switching across the UI, workflow requests, and support chat

## Workflow-First UI Flow

1. The homepage opens on the career workflow intake form.
2. The user submits a JD and candidate profile to `POST /api/career/workflow/analyze`.
3. The same intake can switch to `POST /api/career/workflow/analyze-upload` when a JD or candidate file is attached.
4. The UI renders a structured result view for decision summary, JD analysis, candidate analysis, gap analysis, and interview prep.
5. The user can jump into Support Chat with a prefilled follow-up prompt.
6. Support Chat keeps the current `workflowId` attached so follow-up answers can explain and extend the saved analysis.
7. If a saved `workflowId` exists, the app restores it through `GET /api/career/workflow/{workflowId}` before showing the form again.

## Backend Endpoints

- `GET /api/ai/chat`
- `POST /api/career/workflow/analyze`
- `POST /api/career/workflow/analyze-upload`
- `GET /api/career/workflow/{workflowId}`

## Tech Stack

- Vue 3
- Vite
- Axios
- Native `EventSource` for streaming chat

## Run

```bash
npm install
npm run dev
```

The app expects the backend at `http://localhost:8081/api`.

Workflow persistence is backend-backed in v1, so restored results survive backend restarts as long as the local `./data` directory is preserved.

## Language Support

- Browser locale detection:
  - `zh*` -> `zh-CN`
  - everything else -> `en`
- User preference is stored in local storage under `careerAgent_locale`
- The selected locale is sent through:
  - `POST /api/career/workflow/analyze`
  - `POST /api/career/workflow/analyze-upload`
  - `GET /api/ai/chat`
- Saved workflow artifacts keep their original `contentLocale`
- If the UI locale changes after a workflow is saved, the result view and Support Chat show a notice instead of auto-translating the saved artifact

For local portfolio demos, the backend now defaults to `demo` mode via the `local` Spring profile, so the full workflow can run without external provider keys.

For the canonical AI PM demo pack, quick demo links, and committed screenshots, use the root [README Quick Demo](../README.md#quick-demo) section and the assets under [`docs/demo/ai-pm-canonical`](../docs/demo/ai-pm-canonical/).
