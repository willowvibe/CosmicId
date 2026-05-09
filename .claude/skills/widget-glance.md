# Skill: App Widget (Glance)

## When to use
When modifying or adding home screen widgets.

## Rules
- Widgets use Jetpack Glance — NOT classic RemoteViews
- Widget state: use GlanceStateDefinition backed by DataStore
- Update trigger: use GlanceAppWidgetManager.updateIf<> from a Worker
- Widgets must work with no user interaction (no clicks that require Activity context)
- Always define widget min size in xml/appwidget_info.xml

## File locations
- app/src/main/java/.../widget/
