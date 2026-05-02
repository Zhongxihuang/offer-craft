# AI Career Decision & Interview Prep Agent Frontend

This Vue 3 frontend is the workflow-first UI for the career decision agent.

## Product Role

- Default homepage is the career workflow, not generic chat.
- The main task is: decide whether to apply, identify Top 3 gaps, and generate a 3-7 day prep plan.
- Support Chat is a secondary follow-up workspace linked to the saved `workflowId`.
- The UI supports one-click `中文 | EN` switching.
- Multi-role comparison uses structured role cards, so users never need to remember a `---` text format.

## Workflow-First UI Flow

1. User lands on the workflow intake form.
2. Text-only submissions call `POST /api/career/workflow/analyze`.
3. File submissions call `POST /api/career/workflow/analyze-upload`.
4. Comparison mode calls `POST /api/career/workflow/compare` with 2-5 role cards.
5. Result view highlights apply verdict, confidence, Top 3 gaps, and action plan.
6. Support Chat receives the current `workflowId` for grounded follow-up.
7. Saved workflow restore calls `GET /api/career/workflow/{workflowId}`.

## Backend Endpoints

- `GET /api/ai/chat`
- `POST /api/career/workflow/analyze`
- `POST /api/career/workflow/analyze-upload`
- `POST /api/career/workflow/compare`
- `GET /api/career/workflow/{workflowId}`
- `GET /api/career/workflow/{workflowId}/versions`

## Run

```powershell
npm.cmd install
npm.cmd run dev
```

The frontend expects the backend at `http://localhost:8081/api`.

## Language Support

- Browser locale detection: `zh*` -> `zh-CN`, everything else -> `en`.
- User preference is stored in local storage as `careerAgent_locale`.
- The selected locale is sent with workflow analyze, upload analyze, compare, refine, and support chat requests.
- Saved workflow artifacts keep their original `contentLocale`.
- If the UI language differs from the saved artifact language, the result view and support chat show a notice instead of auto-translating existing output.

## Restore Token

Local demo mode keeps restore frictionless. Provider/prod-like mode can protect restore endpoints with `X-Workflow-Access-Token`.

Frontend token options:

- Build-time: `VITE_WORKFLOW_ACCESS_TOKEN`
- Local storage: `careerAgent_workflowAccessToken`

If restore returns `403`, the UI clears the stale restore attempt and asks the user to rerun analysis or configure a token.

## Build

```powershell
npm.cmd run build
```

For the canonical demo pack, screenshots, and product materials, see the root [README Quick Local Demo](../README.md#quick-local-demo) and [`docs/demo/ai-pm-canonical`](../docs/demo/ai-pm-canonical/).
