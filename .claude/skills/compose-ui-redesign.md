# Skill: Compose UI Redesign

## When to use
When asked to restyle, declutter, or improve any Composable screen.

## Rules
- Never change ViewModel logic or data flows — only touch @Composable functions and theme files
- All spacing must use MaterialTheme.spacing or Dp tokens, no hardcoded values
- Card components: use AgeRevealCard() wrapper, never raw Card() with inline params
- Typography: use MaterialTheme.typography.* only — no hardcoded TextStyle
- Colors: use MaterialTheme.colorScheme.* — no Color(0xFF...) inline
- After any UI change, trigger screenshot walkthrough to capture before/after

## File locations
- Theme: app/src/main/java/.../ui/theme/
- Screens: app/src/main/java/.../ui/screens/
- Components: app/src/main/java/.../ui/components/
