Create a `screenshots/` folder in the project root if it does not already exist.

This walkthrough is **iterative** — if a screenshot file already exists for a screen or
interaction, overwrite it with the latest capture. Never skip a step because a file exists.
Always re-test and re-capture to reflect the current state of the app.

Then perform a full automated UI walkthrough of the app:

1. **Navigate every tab** in the bottom nav / drawer:
   - Capture a screenshot of each tab's default state.
   - If the file already exists, overwrite it.

2. **Interact with every interactive element** on each screen:
   - Tap every button, FAB, icon, and menu item
   - Fill in and submit every form/input field (use valid test data)
   - Trigger every dialog, bottom sheet, and dropdown
   - Screenshot the resulting state after each interaction
   - Overwrite any previously captured screenshot with the same name

3. **Test every feature end-to-end**, including:
   - Happy path (valid inputs, successful responses)
   - Edge cases (empty states, loading states, error/failure responses)
   - Screenshot each distinct response screen, overwriting if it already exists

4. **Bug detection** — while navigating, flag and log any:
   - Crashes or ANRs
   - UI overlaps, clipped text, or broken layouts
   - Missing data / blank screens where content is expected
   - Unresponsive buttons or navigation dead-ends
   - API errors or unexpected error messages
   - Save a screenshot for each bug found with a `BUG_` prefix
     (e.g., `BUG_tab2_submit_crash.png`)
   - **If a previous BUG_ screenshot exists for the same issue and it is now fixed,
     delete the old BUG_ file and note it as resolved in the report**

5. **Screenshot naming convention** (always overwrite on re-run):
   - `tab{N}_{screen_name}_default.png`      — default tab state
   - `tab{N}_{screen_name}_{action}.png`     — after an interaction
   - `BUG_{tab_or_feature}_{description}.png`— active bug only

6. After the walkthrough, **regenerate** `screenshots/REPORT.md` (overwrite if exists):
   - Total screens tested
   - Total interactions tested
   - Bugs still present (with screenshot references)
   - Bugs resolved since last run (previously had BUG_ file, now fixed)
   - Any features that could not be tested and why
   - Timestamp of this run