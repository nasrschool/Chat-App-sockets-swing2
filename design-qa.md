# Design QA

- Source visual truth: `chat_app_visualisation.png`
- Source dimensions: 1518 x 1041 pixels
- Implementation: compiled Java Swing application (`Client.ChatFrame`)
- Intended viewport: 1000 x 680 logical pixels
- State: authenticated main chat screen with a selected conversation
- Implementation screenshot: unavailable
- Density normalization: not applicable because implementation capture was unavailable

## Full-view comparison evidence

The source image was opened at original resolution and used to implement its dark sidebar, light chat surface, blue accent, flat controls, typography hierarchy, subtle separators, and white/light-blue message treatment. The accidental red Stop control was not implemented. A full rendered comparison could not be completed because the Windows computer-use service returned no targetable applications even though the compiled Swing window was running and responsive.

## Focused region comparison evidence

Blocked for the same capture limitation. Code-level inspection covered the sidebar states, title, message bubbles, compose field, and Send button, but code inspection is not a substitute for image comparison.

## Findings

- [P2] Rendered visual comparison unavailable
  - Location: complete Swing chat window.
  - Evidence: source mockup is available, but no implementation screenshot could be captured by the configured computer-use service.
  - Impact: pixel-level font, spacing, and color fidelity cannot be honestly certified.
  - Fix: manually inspect the open client or rerun QA when Windows application capture is available.

## Functional verification

- `mvn clean compile` passed.
- PostgreSQL private boolean values now convert correctly.
- User 1 conversation data reports group 2 as private and derives `User 2` as its display name.
- Existing socket authentication and conversation-list behavior were exercised without changing networking or message-flow code.

## Comparison history

- Initial pass: implementation capture blocked; no P0/P1 findings identified through code inspection, but the capture blocker remains P2.

## Final result

final result: blocked
