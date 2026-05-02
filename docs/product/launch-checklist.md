# Launch Checklist

Use this checklist before a demo, interview, or release-style walkthrough.

## Local Demo

- [ ] JDK 21 is active: `java -version`.
- [ ] Backend starts with no provider keys: `mvn.cmd spring-boot:run`.
- [ ] Frontend builds: `npm.cmd run build`.
- [ ] Frontend dev server opens at `http://localhost:5173`.

## Core Workflow

- [ ] Text-only JD + candidate profile returns a structured workflow result.
- [ ] Result page shows apply verdict, confidence, Top 3 gaps, and 3-7 day action plan.
- [ ] Missing JD returns a visible field error.
- [ ] Missing candidate profile returns a visible field error.
- [ ] Analyze loading state shows staged workflow copy.

## Upload

- [ ] TXT upload works.
- [ ] MD upload works.
- [ ] Text-based PDF upload works.
- [ ] Unsupported file type returns a unified 400 error.
- [ ] Image-only or blank PDF explains that OCR is not supported.

## Restore

- [ ] Refresh restores the latest saved workflow.
- [ ] Backend restart still allows restore by workflowId.
- [ ] With token protection enabled, missing token returns 403.
- [ ] Frontend handles 403 by clearing stale restore and returning to the form.

## Support Chat

- [ ] Result page opens support chat with linked workflow context.
- [ ] Ask mode explains a result without changing the workflow.
- [ ] Update mode explains that it creates a new workflow version.
- [ ] Streaming chat still works.

## Compare

- [ ] Comparison mode uses role cards, not visible `---` syntax.
- [ ] Less than 2 valid roles shows a field-level error.
- [ ] More than 5 roles is blocked.
- [ ] File upload disables comparison with a clear explanation.

## Language

- [ ] Chinese UI is clean and natural.
- [ ] English UI is still usable.
- [ ] Saved artifacts keep their original language.
- [ ] Support chat replies in the current UI language.

## Failure Scenarios

- [ ] Network failure shows a recoverable message.
- [ ] 400, 403, 404, 413, 415, 429, and 500 map to localized frontend messages.
- [ ] Demo mode does not claim external search was used.
- [ ] Provider mode search/RAG failure degrades instead of crashing the workflow.
