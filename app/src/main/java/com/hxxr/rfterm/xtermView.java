package com.hxxr.rfterm;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class xtermView extends View implements GestureDetector.OnGestureListener {

    /**
     * Routine to render a character onto the screen
     * @param c Character to render
     */
    private synchronized void render(char c) {
        // All the escape codes in this file are based on the specification on:
        // http://invisible-island.net/xterm/ctlseqs/ctlseqs.html

        // You can also check escape codes at    http://www.xfree86.org/4.5.0/ctlseqs.html
        // however there is an error on that website.
        // For the description of "CSI u", it incorrectly says "save cursor"
        // while it actually means "restore cursor"

        transcript1.add(c);
        if ((int)c < 32 || (int)c == 127) transcript2.add(c);
        if (!isEscaping && (int)c != 27) transcript3.add(c);
        if (!isEscaping && ((int)c < 32 || (int)c == 127)) transcript4.add(c);

        // If the user has scrolled the window, scroll to the bottom
        if (scrollLines > 0) {
            scrollY = 0;
            scrollLines = 0;
            if (isAlt) canvas.drawBitmap(abitmap, 0, 0, null);
            else canvas.drawBitmap(sbitmap, 0, 0, null);
        }

        if (isEscaping) { // If we are looking for an escape code
            e.add(c); // Add character to escape code buffer

            // Check the code against known list of codes which have length one
            if (e.size() == 1) {
                switch(e.get(0)) {
                    // -----------------------------------------------------------------------------

                    // ESC D   -   Index (IND)
                    // The cursor is moved down one row (without changing cursor column).
                    // The screen is scrolled up if cursor is at last row already.
                    case 'D':
                        logEscape();
                        curY++; // Move cursor to next row

                        if (curY == scrollEnd + 1) { // If cursor is beyond scrolling region
                            // Scroll the screen up one line
                            curY--;
                            scrollUp(1);
                        }

                        // If cursor is still below screen, move it to the last row
                        if (curY >= rows) curY = rows - 1;

                        shouldScroll = true;
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC E   -   Next Line (NEL)
                    // Move the cursor to the first column of the next row.
                    // The screen is scrolled up if cursor is at last row already.
                    case 'E':
                        logEscape();
                        curY++; // Move cursor to next row
                        curX = 0; // Move cursor to start of row

                        if (curY == scrollEnd + 1) { // If cursor is beyond scrolling region
                            // Scroll the screen up one line
                            curY--;
                            scrollUp(1);
                        }

                        // If cursor is still below screen, move it to the last row
                        if (curY >= rows) curY = rows - 1;

                        shouldScroll = true;
                        break;

                    // -----------------------------------------------------------------------------

                    case 'H':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC M   -   Reverse Index (RI)
                    // The cursor is moved up one row (without changing cursor column).
                    // The screen is instead scrolled down if cursor is at first row already.
                    case 'M':
                        logEscape();
                        curY--; // Move cursor to previous row

                        if (curY == scrollStart - 1) { // If cursor is beyond scrolling region
                            // Scroll the screen down one line
                            curY++;
                            scrollDown(1);
                        }

                        // If cursor is still above screen, move it to the first row
                        if (curY < 0) curY = 0;

                        shouldScroll = true;
                        break;

                    // -----------------------------------------------------------------------------

                    case 'N':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'O':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'P':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'V':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'W':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'X':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC 7   -   Save Cursor (DECSC)
                    // This code means save the cursor position.
                    // The next one, ESC 8, can restore the saved cursor position.
                    case '7':
                        logEscape();
                        scurX = curX; // Save cursor X
                        scurY = curY; // Save cursor Y
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC 8   -   Restore Cursor (DECRC)
                    // This code means restore the previously saved cursor position.
                    case '8':
                        logEscape();
                        curX = scurX<cols ? scurX : cols-1; // Restore cursor X
                        curY = scurY<rows ? scurY : rows-1; // Restore cursor Y
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC =   -   Switch to Application Keypad (DECKPAM)
                    // The escape codes to send when keys are pressed are different for
                    // "Application Keypad" and "Normal Keypad"
                    // Check here for more details:
                    // https://www.gnu.org/software/screen/manual/screen.html#Input-Translation
                    // If you are connected to a UNIX shell check your keypad type using
                    // xtermView.isApplicationKeypad() frequently
                    case '=':
                        logEscape();
                        if (!appKeypad) {
                            appKeypad = true;
                            onKeypadChange.run();
                        }
                        break;

                    // -----------------------------------------------------------------------------

                    // ESC >   -   Switch to Normal Keypad (DECKPNM)
                    // The escape codes to send when keys are pressed are different for
                    // "Application Keypad" and "Normal Keypad"
                    // Check here for more details:
                    // https://www.gnu.org/software/screen/manual/screen.html#Input-Translation
                    // If you are connected to a UNIX shell check your keypad type using
                    // xtermView.isApplicationKeypad() frequently
                    case '>':
                        logEscape();
                        if (appKeypad) {
                            appKeypad = false;
                            onKeypadChange.run();
                        }
                        break;

                    // -----------------------------------------------------------------------------

                    case 'F':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'c':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'l':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'm':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'n':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case 'o':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case '|':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case '}':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------

                    case '~':
                        logEscape();
                        break;

                    // -----------------------------------------------------------------------------
                }
            }

            // Detect length-two escape codes that begin with space (SP)
            if (e.size()==2 && e.get(0)==' ')
                logEscape();

            // Detect length-two escape codes that begin with number sign (#)
            if (e.size()==2 && e.get(0)=='#')
                logEscape();

            // Detect length-two escape codes that begin with percent (%)
            if (e.size()==2 && e.get(0)=='%') {
                isEscaping = false;
                Log.d("ViewRoot_ESCAPECODE", String.valueOf(charLToA(e)));
                switch (e.get(1)) {
                    // If the last character is @, set encoding type to default
                    case '@':
                        if (decSpecGraph) {
                            decSpecGraph = false;
                            onDecSpecGraph.run();
                        }
                        break;

                    // If the last character is G, set encoding type to UTF-8
                    case 'G':
                        if (decSpecGraph) {
                            decSpecGraph = false;
                            onDecSpecGraph.run();
                        }
                        break;
                }
                e.clear();
            }

            // The following length-two escape codes are all interpreted identically
            // (they all mean set encoding type):
            // ESC ( C
            // ESC ) C
            // ESC * C
            // ESC + C
            // ESC - C
            // ESC . C
            // ESC / C
            // The "C" represents a character combination consisting of exactly
            // one or two ASCII characters.
            // xtermView enables the DEC special graphics set if C contains only a "0" (zero),
            // otherwise it is disabled.
            if (e.size()>1 &&
                    (e.get(0)=='(' || e.get(0)==')' || e.get(0)=='*' || e.get(0)=='+' ||
                            e.get(0)=='-' || e.get(0)=='.' || e.get(0)=='/')) {
                isEscaping = false;
                Log.d("ViewRoot_ESCAPECODE", String.valueOf(charLToA(e)));
                if (decSpecGraph != (e.get(1)==48)) {
                    decSpecGraph = e.get(1) == 48;
                    onDecSpecGraph.run();
                }
                e.clear();
            }

            // --------------------------------------------------------------------------------------

            // Check for CSI and OSC codes only if escape code buffer length is at least 2
            else if (e.size()>1) {

                // If the first character is a "[", it's a CSI code
                if (e.get(0) == '[' && (byte)e.get(e.size() - 1).charValue() > 63) {
                    isEscaping = false;
                    Log.d("ViewRoot_ESCAPECODE", String.valueOf(charLToA(e)));

                    // Determine the type of CSI code by looking at the last character
                    switch (e.get(e.size() - 1)) {
                        // -------------------------------------------------------------------------

                        // ESC [ Ps @   -   Insert Characters (ICH)
                        // If the code ends in "@", it means insert a blank character at the cursor
                        // position. Additionally, (Ps - 1) blank characters are inserted to the
                        // right of the cursor. Characters that were in the original positions are
                        // shifted to the right, not overwritten. If no parameter is specified, only
                        // one blank character will be inserted at the cursor position.
                        case '@':
                            int offset_AT;

                            // If a parameter was specified,
                            // make the amount of characters to addequal to the parameter
                            if (parsePs(charLToA(e)) != -1) offset_AT = parsePs(charLToA(e));

                                // If no parameter was specified,
                                // make the amount of characters to remove equal to 1
                            else offset_AT = 1;

                            // First shift the characters to the right of
                            // the characters to be overwritten to the right
                            bShift(curY, curX, cols - curX - offset_AT, offset_AT);

                            // Overwrite the characters to be overwritten with spaces
                            bClear(curY, curX, offset_AT);

                            redrawRow(curY); // Redraw the row we just modified
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps A   -   Cursor Up (CUU)
                        // If the code ends in "A", it means move the cursor upwards Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        // This code is exactly the same as "ESC [ Ps F".
                        case 'A':
                            int LA = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "A")
                            if (LA > 0)
                                // Move cursor upwards the number of times specified by the
                                // parameter
                                curY = Math.max(0, curY-LA);

                                // If no parameters were detected, move cursor upwards once
                            else if (curY > 0) curY--;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps B   -   Cursor Down (CUD)
                        // If the code ends in "B", it means move the cursor downwards Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        // This code is exactly the same as "ESC [ Ps E".
                        case 'B':
                            int LB = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "B")
                            if (LB > 0)
                                // Move cursor downwards the number of times specified by the
                                // parameter
                                curY = Math.min(rows-1, curY+LB);

                                // If no parameters were detected, move cursor downwards once
                            else if (curY < rows) curY++;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps C   -   Cursor Right (CUF)
                        // If the code ends in "C", it means move the cursor to the right Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        case 'C':
                            int LC = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "C")
                            if (LC > 0) {
                                // Move cursor to the right the number of times specified by the
                                // parameter
                                curX = Math.min(cols-1, curX+LC);
                            }

                            // If no parameters were detected, move cursor to the right once
                            else if (curX < cols) curX++;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps D   -   Cursor Left (CUB)
                        // If the code ends in "D", it means move the cursor to the left Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        case 'D':
                            int LD = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "D")
                            if (LD > 0) {
                                // Move cursor to the left the number of times specified by the
                                // parameter
                                curX = Math.max(0, curX-LD);
                            }

                            // If no parameters were detected, move cursor to the left once
                            else if (curX > 0) curX--;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps E   -   Cursor Next Line (CNL)
                        // If the code ends in "E", it means move the cursor downwards Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        // This code is exactly the same as "ESC [ Ps B".
                        case 'E':
                            int LE = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "E")
                            if (LE > 0)
                                // Move cursor downwards the number of times specified by the
                                // parameter
                                curY = Math.min(rows-1, curY+LE);

                                // If no parameters were detected, move cursor downwards once
                            else if (curY < rows) curY++;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps F   -   Cursor Previous Line (CPL)
                        // If the code ends in "F", it means move the cursor upwards Ps times.
                        // If Ps is not specified, the cursor is moved once.
                        // This code is exactly the same as "ESC [ Ps A".
                        case 'F':
                            int LF = parsePs(charLToA(e));
                            // If a parameter was detected (any number of NUMBERS before the "A")
                            if (LF > 0)
                                // Move cursor upwards the number of times specified by the parameter
                                curY = Math.max(0, curY-LF);

                                // If no parameters were detected, move cursor upwards once
                            else if (curY > 0) curY--;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm G   -   Column Position Absolute (CHA)
                        // If the code ends in "G", it means set the column the cursor is in.
                        // If at least one parameter is given, the column the cursor is in is set to
                        // the first parameter. If no parameters are given, the cursor is moved to
                        // the leftmost column. Note that parameters are one-based (NOT zero-based),
                        // so 1 is the first column, not 0.
                        // This code is identical to "ESC [ Pm `".
                        case 'G':
                            int Pm_G[] = parsePm(charLToA(e));

                            // If at least one numeric parameter is given, set column depending on
                            // parameter
                            if (Pm_G.length > 0) {
                                if (Pm_G[0] <= cols) curX = Pm_G[0] - 1;
                                else curX = cols - 1;
                            }

                            // If no parameters are given, move cursor to first column
                            else curX = 0;

                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps ; Ps H   -   Cursor Position Absolute (CUP)
                        // If the code ends in "H", it means set the cursor position.
                        // If two numeric parameters are given,
                        // et the cursor position (Ps ; Ps  =  row ; column).
                        // If no parameters are given (  i.e. ESC [ H  ), set cursor position to
                        // leftmost column of top row. Note that parameters are
                        // one-based (NOT zero-based), so 1 is the first row, not 0.
                        // Also note that this is actually exactly the same as "ESC [ Ps ; Ps f".
                        case 'H':
                            int Pm_H[] = parsePm(charLToA(e));
                            shouldScroll = false;

                            // If exactly two numeric parameters are given, set cursor position
                            // depending on parameters
                            if (Pm_H.length == 2) {
                                if (Pm_H[0] <= rows && Pm_H[0] > 0) curY = Pm_H[0] - 1;
                                else if (Pm_H[0] < 1) curY = 0;
                                else curY = rows - 1;
                                if (Pm_H[1] <= cols && Pm_H[1] > 0) curX = Pm_H[1] - 1;
                                else if (Pm_H[1] < 1) curX = 0;
                                else curX = cols - 1;
                            }

                            // If no parameters are given, set cursor position to
                            // leftmost column of top row
                            if (Pm_H.length == 0) {
                                curY = 0;
                                curX = 0;
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps J   -   Erase in Display (ED)
                        // If the code ends in "J", erase part of or all of the current screen
                        // buffer.
                        // Ps can only be equal to 0, 1, 2 or 3.
                        // If Ps is equal to 0, all characters in rows below the
                        // cursor's row are erased.
                        // If Ps is equal to 1, all characters in rows above the
                        // cursor's row are erased.
                        // In this case, characters in the cursor's row to the left of
                        // the cursor will also be erased.
                        // If Ps is equal to 2, if we are using Normal Screen Buffer, the contents
                        // of the screen are moved to the scrollback, then the screen is cleared.
                        // If we're using Alternate Screen Buffer the screen is cleared without
                        // moving to scrollback.

                        // If Ps is equal to 3, the scrollback is cleared, however the screen
                        // is not. This happens regardless of which screen buffer we're using.
                        case 'J':
                            int mode_J;
                            if (e.get(1) != '?') {
                                // If a parameter was specified, make the mode (0, 1, 2, 3) equal to
                                // the parameter
                                if (parsePs(charLToA(e)) != -1) mode_J = parsePs(charLToA(e));
                                    // If no parameter was specified, make the mode equal to 0
                                else mode_J = 0;
                            } else {
                                // If a parameter was specified, make the mode (0, 1, 2, 3) equal to
                                // the parameter
                                if (parsePs(Arrays.copyOfRange(charLToA(e), 1, e.size()))!=-1)
                                    mode_J = parsePs(charLToA(e));
                                    // If no parameter was specified, make the mode equal to 0
                                else mode_J = 0;
                            }

                            switch (mode_J) {
                                // If the mode is 0, erase characters below the cursor
                                case 0:
                                    if (curY < rows-1) {
                                        bClear(curY+1, 0, cols*(rows-curY-1));
                                        canvas.drawRect(0, paddingY+(curY+1)*charHeight,
                                                getWidth(), getHeight(), dpaint);
                                    }
                                    break;

                                // If the mode is 1, erase characters above and to the
                                // left of cursor
                                case 1:
                                    if (curY < 1)
                                        bClear(0, 0, curX);
                                    else {
                                        bClear(0, 0, cols*curY + curX);
                                        canvas.drawRect(0, 0, getWidth(),
                                                paddingY+(curY)*charHeight, dpaint);
                                    }

                                    if (curX > 0)
                                        cBlockPaint(0, curY, curX-1, curY, dpaint);
                                    break;

                                // If the mode is 2, clear screen
                                case 2:
                                    // If we're using Normal Screen Buffer, first push to scrollback
                                    if (!isAlt) {
                                        for (int i = 0; i < rows; i++)
                                            scrollback.add(0, bitCrop(i, i));
                                        if (scrollback.size() > Global.getInstance().maxScrollback)
                                            scrollback.remove(scrollback.size()-1);
                                    }
                                    // Clear screen
                                    bClear(0, 0, rows*cols);
                                    canvas.drawColor(colors[1]);
                                    break;

                                // If the mode is 3, clear scrollback
                                case 3:
                                    scrollback.clear();
                                    break;
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps K   -   Erase in Line (EL)
                        // If the code ends in "K", erase part of or all of the current line.
                        // Ps can only be equal to 0, 1 or 2.
                        // If Ps is equal to 0, the character the cursor is on is erased as well as
                        // characters to the right of the cursor.
                        // If Ps is equal to 1, characters to the left of the cursor are erased.
                        // If Ps is equal to 2, all characters in the line are erased.
                        // If Ps is not specified, it is treated as if it was equal to 0.
                        // Please note that this code does not shift in characters like
                        // "Delete Characters" does, or move the cursor.
                        // Also, "erasing" means painting over cells with the
                        // CURRENT BACKGROUND COLOUR, not the default background colour.
                        case 'K':
                            int mode_K;
                            if (e.get(1) != '?') {
                                // If a parameter was specified, make the mode (0, 1 or 2) equal to
                                // the parameter
                                if (parsePs(charLToA(e)) != -1) mode_K = parsePs(charLToA(e));
                                    // If no parameter was specified, make the mode equal to 0
                                else mode_K = 0;
                            } else {
                                // If a parameter was specified, make the mode (0, 1 or 2) equal to
                                // the parameter
                                if (parsePs(Arrays.copyOfRange(charLToA(e), 1, e.size()))!=-1)
                                    mode_K = parsePs(charLToA(e));
                                    // If no parameter was specified, make the mode equal to 0
                                else mode_K = 0;
                            }

                            switch (mode_K) {
                                // If the mode is 0, erase current character and all characters to
                                // the right of the cursor
                                case 0:
                                    bClearE(curY, curX, cols - curX);
                                    cBlockPaint(curX, curY, cols-1, curY, bpaint);
                                    break;

                                // If the mode is 1, erase all characters to the left of the cursor
                                case 1:
                                    bClearE(curY, 0, curX);
                                    cBlockPaint(0, curY, curX-1, curY, bpaint);
                                    break;

                                // If the mode is 2, erase all characters in the same row as
                                // the cursor
                                case 2:
                                    bClearE(curY, 0, cols);
                                    cBlockPaint(0, curY, cols-1, curY, bpaint);
                                    break;
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps L   -   Insert Lines (IL)
                        // If the code ends in "L",
                        // shift all rows in the SCROLLING REGION, except for the rows above the
                        // cursor, downwards Ps times.
                        // Rows that are shifted to below the scrolling region are deleted.
                        // If Ps is not specified, Ps is made equal to 1.
                        // Despite shifting rows, this escape code does not move the cursor.
                        // If the cursor is in the last row of the scrolling region or outside the
                        // scrolling region this code is ignored.

                        case 'L':
                            // Ensure that cursor is not in last row of scrolling region or outside
                            // scrolling region
                            if (curY >= scrollStart && curY < scrollEnd) {

                                // If no parameter was specified, make the parameter equal to 1
                                int offset_L = (parsePs(charLToA(e))>0) ? parsePs(charLToA(e)) : 1;

                                // If the parameter is greater than the amount of rows in the
                                // scrolling region below the cursor row,
                                // just clear cursor row and rows below cursor in scrolling region
                                if (offset_L > scrollEnd - curY) {
                                    bClear(curY, 0, cols*(scrollEnd-curY+1));
                                    cBlockPaint(0, curY, cols-1, scrollEnd, dpaint);
                                }

                                // If not, actually perform the shifting operation
                                else {
                                    // Shift current cursor row and rows below cursor down Ps times
                                    bShift(curY, 0, cols*(scrollEnd-curY-offset_L+1),
                                            cols*offset_L);
                                    cBlockShift(0, curY, cols-1,
                                            scrollEnd-offset_L, offset_L, 0);

                                    // Clear the duplicate rows which are supposed to be blank
                                    bClear(curY, 0, cols*offset_L);
                                    cBlockPaint(0, curY, cols-1, curY+offset_L-1,
                                            dpaint);
                                }
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps M   -   Delete Lines (DL)
                        // If the code ends in "M", delete the cursor row, as well as
                        // (Ps - 1) rows below the cursor row.
                        // After the rows are deleted,
                        // rows in the SCROLLING REGION below the deleted rows are shifted upwards
                        // to fill the gap.
                        // If Ps is not specified, Ps is made equal to 1.
                        // Despite shifting rows, this escape code does not move the cursor.
                        // If the cursor is in the last row of the scrolling region or outside the
                        // scrolling region this code is ignored.

                        case 'M':
                            // Ensure that cursor is not in last row of scrolling region or
                            // outside scrolling region
                            if (curY >= scrollStart && curY < scrollEnd) {

                                // If no parameter was specified, make the parameter equal to 1
                                int offset_M = (parsePs(charLToA(e))>0) ? parsePs(charLToA(e)) : 1;

                                // If the parameter is greater than the amount of rows in the
                                // scrolling region below the cursor row,
                                // just clear cursor row and rows below cursor in scrolling region
                                if (offset_M > scrollEnd - curY) {
                                    bClear(curY, 0, cols*(scrollEnd-curY+1));
                                    cBlockPaint(0, curY, cols-1, scrollEnd, dpaint);
                                }

                                // If not, actually perform the shifting operation
                                else {
                                    // Clear the cursor row as well as (Ps - 1) rows below the
                                    // cursor row
                                    bClear(curY, 0, cols*offset_M);

                                    // Shift rows in scrolling region below
                                    // deleted rows upwards Ps times
                                    bShift(curY+offset_M, 0,
                                            cols*(scrollEnd-curY-offset_M+1),
                                            -cols*offset_M);
                                    cBlockShift(0, curY+offset_M, cols-1, scrollEnd,
                                            -offset_M, 0);

                                    // Clear the duplicate rows which are supposed to be blank
                                    bClear(scrollEnd-offset_M+1, 0,
                                            cols*offset_M);
                                    cBlockPaint(0, scrollEnd-offset_M+1,
                                            cols-1, scrollEnd, dpaint);
                                }
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps P   -   Delete Characters (DCH)
                        // If the code ends in "P", remove the character the cursor is on AND
                        // (Ps - 1) characters to the right of the cursor.
                        // If Ps is not specified or if Ps is equal to 1,
                        // only the character the cursor is on is removed.
                        // After the characters are removed, remaining characters to the right are
                        // shifted in to fill the gap.
                        case 'P':
                            // If no parameter was specified, make the amount of
                            // characters to remove equal to 1
                            int offset_P = (parsePs(charLToA(e)) > 0) ? parsePs(charLToA(e)) : 1;

                            // Clip parameter value if it means deleting
                            // characters outside of the current row
                            offset_P = Math.min(cols - curX, offset_P);

                            // Remove the specified amount of characters by changing them to spaces
                            bClear(curY, curX, offset_P);

                            // Move the characters to the right of the cursor inwards to
                            // fill the gap
                            if (curX + offset_P < rows)
                                bShift(curY, curX + offset_P, cols-curX-offset_P,
                                        -offset_P);

                            // Remove any leftover characters at the end of the row
                            bClear(curY, cols - offset_P, offset_P);

                            redrawRow(curY); // Redraw the row we just modified
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps S   -   Scroll Up (SU)
                        // If the code ends in "S", scroll up by the number of lines specified by
                        // the parameter.
                        // If no parameter is specified, scroll up by one line.
                        case 'S':
                            int offset_S = parsePs(charLToA(e));

                            // If no parameter was specified, set the parameter to 1
                            if (offset_S < 1) offset_S = 1;

                            // Scroll up by the specified number of lines
                            scrollUp(offset_S);
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps T   -   Scroll Down (SD)
                        // If the code ends in "T" and does not contain a ">" and has
                        // exactly one parameter,
                        // scroll down by the number of lines specified by the parameter.
                        // If no parameter is specified, scroll down by one line.
                        case 'T':
                            if (e.get(1)!='>' && parsePm(charLToA(e)).length==1) {
                                int offset_T = parsePs(charLToA(e));

                                // If no parameter was specified, set the parameter to 1
                                if (offset_T < 0) offset_T = 1;

                                // Scroll down by the specified number of lines
                                scrollDown(offset_T);
                            }


                            // ---------------------------------------------------------------------

                            // ESC [ Ps ; Ps ; Ps ; Ps T   -   Enable Highlight Cursor Tracking
                            // If the code ends in "T" and does not contain a ">" and has
                            // exactly four parameters,
                            // enable highlight cursor tracking.
                            // TODO: ENABLE HIGHLIGHT CURSOR TRACKING
                            else if (e.get(1)!='>' && parsePm(charLToA(e)).length==4) {

                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps X   -   Erase Characters (EC)
                        // If the code ends in "X", erase the character at the cursor position.
                        // If Ps is specified and is greater than 1,
                        // (Ps - 1) additional characters to the right of the cursor are erased.
                        // If Ps is not specified or equal to 0 or 1, only the character at the
                        // cursor position is erased.
                        // No characters are shifted by this operation and the cursor does not move.
                        // Also, "erasing" means painting over cells with the
                        // CURRENT BACKGROUND COLOUR, not the default background colour.
                        case 'X':
                            // If no parameter is specified, set it to 1
                            int Ps_X = parsePs(charLToA(e))>0 ? parsePs(charLToA(e)) : 1;

                            // Clip the value of the parameter if it means
                            // erasing more characters than there are in the current row
                            Ps_X = Math.min(cols - curX, Ps_X);

                            // Erase characters
                            bClearE(curY, curX, Ps_X);
                            cBlockPaint(curX, curY, curX+Ps_X-1, curY, bpaint);
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps ^   -   Scroll Down (SD)
                        // If the code ends in "^", scroll down by the number of lines specified by
                        // the parameter.
                        // If no parameter is specified, scroll down by one line.
                        // This code is identical to "ESC [ Ps T".
                        case '^':
                            int offset_caret = parsePs(charLToA(e));

                            // If no parameter was specified, set the parameter to 1
                            if (offset_caret < 0) offset_caret = 1;

                            // Scroll down by the specified number of lines
                            scrollDown(offset_caret);
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm `   -   Character Position Absolute (HPA)
                        // If the code ends in "`", it means set the column the cursor is in.
                        // If at least one parameter is given, the column the cursor is in is set to
                        // the first parameter.
                        // If no parameters are given, the cursor is moved to the leftmost column.
                        // Note that parameters are one-based (NOT zero-based), so 1 is the
                        // first column, not 0.
                        // This code is identical to "ESC [ Pm G".
                        case '`':
                            int Pm_grave[] = parsePm(charLToA(e));

                            // If at least one numeric parameter is given, set column depending on
                            // parameter
                            if (Pm_grave.length > 0) {
                                if (Pm_grave[0] <= cols) curX = Pm_grave[0] - 1;
                                else curX = cols - 1;
                            }

                            // If no parameters are given, move cursor to first column
                            else curX = 0;

                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm a   -   Character Position Relative (HPR)
                        // If the code ends in "a", it means move the cursor to the right.
                        // If at least one parameter is given, the first parameter is the number of
                        // times to move the cursor to the right.
                        // If no parameter is specified, the cursor is moved to the right once.
                        case 'a':
                            int Pm_a[] = parsePm(charLToA(e));

                            // If at least one parameter was given
                            if (Pm_a.length > 0 && Pm_a[0] > 0)
                                // Move cursor to the right the number of times specified by the
                                // first parameter
                                curX = Math.min(cols-1, curX+Pm_a[0]);

                                // If no parameters were detected, move cursor to the right once
                            else if (curX < cols) curX++;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps c   -   Send Primary Device Attributes
                        // If the code ends in "c" and does not contain a ">",
                        // send terminal information.
                        // If a parameter is given and is not zero (0), this code is ignored.
                        // If the parameter is zero (0) or not given,
                        // xtermView sends back one of the
                        // following escape codes indicating terminal type:
                        // ESC [ ? 1 ; 0 c                     (VT100 without Advanced Video)
                        // ESC [ ? 1 ; 2 c                     (VT100 with Advanced Video)
                        // ESC [ ? 6 c                         (VT102)
                        // ESC [ ? 6 2 ; 2 2 c                 (VT200)
                        // ESC [ ? 6 3 ; 2 2 c                 (VT320)
                        // ESC [ ? 6 4 ; 2 2 c                 (VT420)
                        case 'c':
                            if (e.get(1)!='>' && parsePs(charLToA(e))<1)
                                shell.output(new char[]{27,'[','?','6','4',';','2','2','c'});
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm d   -   Line Position Absolute (VPA)
                        // If the code ends in "d", it means set the row the cursor is in.
                        // If at least one parameter is given, the row the cursor is in is set to
                        // the first parameter.
                        // If no parameters are given, the cursor is moved to the top row.
                        // Note that parameters are one-based (NOT zero-based),
                        // so 1 is the first row, not 0.
                        case 'd':
                            int Pm_d[] = parsePm(charLToA(e));

                            // If at least one numeric parameter is given, set row depending on
                            // parameter
                            if (Pm_d.length > 0) {
                                if (Pm_d[0] <= rows) curY = Pm_d[0] - 1;
                                else curY = rows - 1;
                            }

                            // If no parameters are given, move cursor to first row
                            else curY = 0;

                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm e   -   Line Position Relative (VPR)
                        // If the code ends in "e", it means move the cursor downwards.
                        // If at least one parameter is given, the first parameter is the number of
                        // times to move the cursor downwards.
                        // If no parameter is specified, the cursor is moved downwards once.
                        case 'e':
                            int Pm_e[] = parsePm(charLToA(e));

                            // If at least one parameter was given
                            if (Pm_e.length > 0 && Pm_e[0] > 0)
                                // Move cursor downwards the number of times specified by the
                                // first parameter
                                curY = Math.min(rows - 1, curY + Pm_e[0]);

                                // If no parameters were detected, move cursor downwards once
                            else if (curY < rows) curY++;
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps ; Ps f   -   Horizontal and Vertical Position (HVP)
                        // If the code ends in "f", it means set the cursor position.
                        // If two numeric parameters are given,
                        // set the cursor position (Ps ; Ps  =  row ; column).
                        // If no parameters are given (  i.e. ESC [ f  ), set cursor position to
                        // leftmost column of top row.
                        // Note that parameters are one-based (NOT zero-based),
                        // so 1 is the first row, not 0.
                        // Also note that this is actually exactly the same as "ESC [ Ps ; Ps H".
                        case 'f':
                            int Pm_f[] = parsePm(charLToA(e));
                            shouldScroll = false;

                            // If exactly two numeric parameters are given,
                            // set cursor position depending on parameters
                            if (Pm_f.length == 2) {
                                if (Pm_f[0] <= rows && Pm_f[0] > 0) curY = Pm_f[0] - 1;
                                else if (Pm_f[0] < 1) curY = 0;
                                else curY = rows - 1;
                                if (Pm_f[1] <= cols && Pm_f[1] > 0) curX = Pm_f[1] - 1;
                                else if (Pm_f[1] < 1) curX = 0;
                                else curX = cols - 1;
                            }

                            // If no parameters are given, set cursor position to leftmost column of
                            // top row
                            if (Pm_f.length == 0) {
                                curY = 0;
                                curX = 0;
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm h   -   Set Mode (SM)
                        case 'h':
                            if (e.get(1)!='?') {
                                // Iterate through every parameter given
                                for (int p : parsePm(charLToA(e))) {
                                    switch (p) {

                                        // If the parameter is 20, enable automatic newline
                                        // (interprets every \n as \r\n)
                                        case 20:
                                            autoNewline = true;
                                            break;
                                    }

                                }
                            }

                            // ---------------------------------------------------------------------

                            // ESC [ ? Pm h   -   DEC Private Mode Set (DECSET)
                            else {
                                // Iterate through every parameter given
                                for (int p : parsePm(charLToA(e))) {
                                    switch (p) {

                                        // If the parameter is 1,
                                        // switch to Application Cursor Keys (DECCKM).
                                        case 1:
                                            if (!appCursorKeys) {
                                                appCursorKeys = true;
                                                onCursorKeysChange.run();
                                            }
                                            break;

                                        // If the parameter is 9,
                                        // enable X10 Compatibility Mode Mouse Tracking.
                                        case 9:
                                            if (!mouseTracking_x10compat) {
                                                mouseTracking_x10compat = true;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 25, show cursor (DECTCEM).
                                        case 25:
                                            if (!cursorShown) {
                                                cursorShown = true;
                                                onCursorShownHidden.run();
                                            }
                                            break;

                                        // If the parameter is 47,
                                        // switch to Alternate Screen Buffer
                                        // without saving cursor position.
                                        // This is equivalent to 1047.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 47:
                                            if (!isAlt && !res_titeInhibit) {
                                                scrollStart = ascrollStart;
                                                scrollEnd = ascrollEnd;
                                                isAlt = true; // Use Alternate Screen Buffer
                                                onScreenBufferChange.run();
                                                restoreBuffer(); // Redraw screen
                                            }
                                            break;

                                        // If the parameter is 1000, enable Normal Mouse Tracking.
                                        case 1000:
                                            if (!mouseTracking_normal) {
                                                mouseTracking_normal = true;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1002,
                                        // enable Button-Event Mouse Tracking.
                                        case 1002:
                                            if (!mouseTracking_button) {
                                                mouseTracking_button = true;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1003,
                                        // enable Any-Event Mouse Tracking.
                                        case 1003:
                                            if (!mouseTracking_any) {
                                                mouseTracking_any = true;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1047,
                                        // switch to Alternate Screen Buffer
                                        // without saving cursor position.
                                        // This is equivalent to 47.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1047:
                                            if (!isAlt && !res_titeInhibit) {
                                                scrollStart = ascrollStart;
                                                scrollEnd = ascrollEnd;
                                                isAlt = true; // Use Alternate Screen Buffer
                                                onScreenBufferChange.run();
                                                restoreBuffer(); // Redraw screen
                                            }
                                            break;

                                        // If the parameter is 1048, save the cursor position.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1048:
                                            if (!res_titeInhibit) {
                                                acurX = curX; // Save cursor X
                                                acurY = curY; // Save cursor Y
                                            }
                                            break;

                                        // If the parameter is 1049, save the cursor position,
                                        // then switch to Alternate Screen Buffer.
                                        // This code also clears the Alternate Screen Buffer if
                                        // the Normal Screen Buffer was active.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1049:
                                            if (!res_titeInhibit) {
                                                acurX = curX; // Save cursor X
                                                acurY = curY; // Save cursor Y
                                                if (!isAlt) {
                                                    // Clear Alternate Screen Buffer
                                                    bClear(0, 0, rows*cols);
                                                    // Clear Alternate Screen Buffer bitmap
                                                    acanvas.drawColor(colors[1]);
                                                    scrollStart = ascrollStart;
                                                    scrollEnd = ascrollEnd;
                                                    isAlt = true; // Use Alternate Screen Buffer
                                                    onScreenBufferChange.run();
                                                    restoreBuffer(); // Redraw screen
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm l   -   Reset Mode (SM)
                        // TODO: RESET MODE (RM)
                        case 'l':
                            if (e.get(1)!='?') {
                                // Iterate through every parameter given
                                for (int p : parsePm(charLToA(e))) {
                                    switch (p) {

                                        // If the parameter is 20, disable automatic newline
                                        case 20:
                                            autoNewline = false;
                                            break;
                                    }

                                }
                            }

                            // ---------------------------------------------------------------------

                            // ESC [ ? Pm l   -   DEC Private Mode Reset (DECRST)
                            else {
                                // Iterate through every parameter given
                                for (int p : parsePm(charLToA(e))) {
                                    switch (p) {

                                        // If the parameter is 1,
                                        // switch to Normal Cursor Keys (DECCKM).
                                        case 1:
                                            if (appCursorKeys) {
                                                appCursorKeys = false;
                                                onCursorKeysChange.run();
                                            }
                                            break;

                                        // If the parameter is 9,
                                        // disable X10 Compatibility Mode Mouse Tracking.
                                        case 9:
                                            if (mouseTracking_x10compat) {
                                                mouseTracking_x10compat = false;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 25, hide cursor (DECTCEM).
                                        case 25:
                                            if (cursorShown) {
                                                cursorShown = false;
                                                onCursorShownHidden.run();
                                            }
                                            break;

                                        // If the parameter is 47,
                                        // switch back to Normal Screen Buffer
                                        // without restoring cursor position.
                                        // Unlike 1047,
                                        // the Alternate Screen Buffer is NOT cleared if
                                        // currently active.
                                        // This escape code is NOT disabled by titeInhibit.
                                        case 47:
                                            if (isAlt) {
                                                scrollStart = ascrollStart;
                                                scrollEnd = ascrollEnd;
                                                isAlt = false; // Use Normal Screen Buffer
                                                onScreenBufferChange.run();
                                                restoreBuffer(); // Redraw screen
                                            }
                                            break;

                                        // If the parameter is 1000, disable Normal Mouse Tracking.
                                        case 1000:
                                            if (mouseTracking_normal) {
                                                mouseTracking_normal = false;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1002,
                                        // disable Button-Event Mouse Tracking.
                                        case 1002:
                                            if (mouseTracking_button) {
                                                mouseTracking_button = false;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1003,
                                        // disable Any-Event Mouse Tracking.
                                        case 1003:
                                            if (mouseTracking_any) {
                                                mouseTracking_any = false;
                                                onMouseTrackingChange.run();
                                            }
                                            break;

                                        // If the parameter is 1047,
                                        // switch back to Normal Screen Buffer
                                        // without restoring cursor position.
                                        // If the Alternate Screen Buffer is currently active,
                                        // it is cleared.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1047:
                                            if (isAlt && !res_titeInhibit) {
                                                // Clear Alternate Screen Buffer
                                                bClear(0, 0, rows*cols);
                                                // Clear Alternate Screen Buffer bitmap
                                                acanvas.drawColor(colors[1]);
                                                scrollStart = ascrollStart;
                                                scrollEnd = ascrollEnd;
                                                isAlt = false; // Use Normal Screen Buffer
                                                onScreenBufferChange.run();
                                                restoreBuffer(); // Redraw screen
                                            }
                                            break;

                                        // If the parameter is 1048,
                                        // restore previously saved cursor position.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1048:
                                            // Restore cursor X
                                            curX = acurX<cols&&!res_titeInhibit ? acurX : cols-1;
                                            // Restore cursor Y
                                            curY = acurY<rows&&!res_titeInhibit ? acurY : rows-1;
                                            break;

                                        // If the parameter is 1049,
                                        // switch back to Normal Screen Buffer AND
                                        // restore cursor position.
                                        // If the Alternate Screen Buffer is currently active,
                                        // it is cleared.
                                        // If titeInhibit is enabled this escape code is ignored.
                                        case 1049:
                                            // Restore cursor X
                                            curX = acurX<cols&&!res_titeInhibit ? acurX : cols-1;
                                            // Restore cursor Y
                                            curY = acurY<rows&&!res_titeInhibit ? acurY : rows-1;

                                            if (isAlt && !res_titeInhibit) {
                                                // Clear Alternate Screen Buffer
                                                bClear(0, 0, rows*cols);
                                                // Clear Alternate Screen Buffer bitmap
                                                acanvas.drawColor(colors[1]);
                                                scrollStart = ascrollStart;
                                                scrollEnd = ascrollEnd;
                                                isAlt = false; // Use Normal Screen Buffer
                                                onScreenBufferChange.run();
                                                restoreBuffer(); // Redraw screen
                                            }
                                            break;
                                    }
                                }
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Pm m   -   Character Attributes
                        // TODO: FINISH COLOURS
                        // If the code ends in "m" and does not contain a ">",
                        // it's a character attributes code (colour, bold, italic, etc...)
                        case 'm':
                            if (e.get(0) != '>') {
                                int Pm_m[] = parsePmC(charLToA(e));
                                boolean iso = false;

                                // Iterate through every parameter given
                                for (int p : Pm_m) {

                                    // If previous parameter was 38 or 48 and current parameter is
                                    // 2 or 5, stop checking parameters
                                    if (iso) {
                                        if (p == 2 || p == 5)
                                            break;
                                        else
                                            iso = false;
                                    }

                                    // If the parameter is 0,
                                    // clear all text formatting and colouring
                                    if (p == 0) {
                                        if (isReversed) {
                                            int _fpaint = fpaint.getColor();
                                            fpaint.setColor(bpaint.getColor());
                                            bpaint.setColor(_fpaint);
                                            isReversed = false;
                                        }
                                        typeface = 0;
                                        setF(-3);
                                        setB(-2);
                                    }

                                    // If the parameter is 1 or 5, make the text bold
                                    if (p == 1 || p == 5) typeface = 1;

                                    // If the parameter is 7, enable reverse video
                                    // (swap foreground and background colours)
                                    if (p == 7 && !isReversed) {
                                        int _fpaint = fpaint.getColor();
                                        fpaint.setColor(bpaint.getColor());
                                        bpaint.setColor(_fpaint);
                                        isReversed = true;
                                    }

                                    // If the parameter is 22, disable bold text
                                    // (bold is enabled by parameters 1 and 5)
                                    if (p == 22) typeface = 0;

                                    // If the parameter is 27, disable reverse video
                                    // (reverse video is enabled by parameter 7)
                                    if (p == 27 && isReversed) {
                                        int _fpaint = fpaint.getColor();
                                        fpaint.setColor(bpaint.getColor());
                                        bpaint.setColor(_fpaint);
                                        isReversed = false;
                                    }

                                    // If the parameter is between 30 and 37,
                                    // set foreground colour depending on parameter
                                    if (p > 29 && p < 38) setF(p - 30);

                                    // If the parameter is 38 or 48,
                                    // check if the next parameter is 2 or 5
                                    if (p == 38 || p == 48) iso = true;

                                    // If the parameter is 39,
                                    // set foreground colour to default foreground colour
                                    if (p == 39) setF(-3);

                                    // If the parameter is between 40 and 47,
                                    // set background colour depending on parameter
                                    if (p > 39 && p < 48) setB(p - 40);

                                    // If the parameter is 49,
                                    // set foreground colour to default foreground colour
                                    if (p == 49) setB(-2);

                                    // If the parameter is between 90 and 97,
                                    // set foreground colour (bright) depending on parameter
                                    if (p > 89 && p < 98) setF(p - 82);

                                    // If the parameter is between 100 and 107,
                                    // set background colour (bright) depending on parameter
                                    if (p > 99 && p < 108) setB(p - 92);
                                }

                                // If no parameters were given,
                                // clear all text formatting and colouring
                                if (e.size() < 3) {
                                    if (isReversed) {
                                        int _fpaint = fpaint.getColor();
                                        fpaint.setColor(bpaint.getColor());
                                        bpaint.setColor(_fpaint);
                                        isReversed = false;
                                    }
                                    typeface = 0;
                                    setF(-3);
                                    setB(-2);
                                }

                                // ESC [ 38 ; 2 ; Pi ; Pr ; Pg ; Pb m
                                // This code indicates to set the foreground colour to the
                                // closest match in the 256 colour palette. Pr, Pg and Pb
                                // indicate the red, green and blue components of the colour,
                                // and Pi is ignored.
                                // All parameters must be integer values from 0 to 255.
                                // If directColor is enabled, this code instead sets the foreground
                                // colour to the specified colour rather than looking for the
                                // closest colour in the palette.
                                if (Pm_m.length==6 && Pm_m[0]==38 && Pm_m[1]==2) {

                                    // Check that R, G and B values are all below 256
                                    if (Pm_m[3]<256 && Pm_m[4]<256 && Pm_m[5]<256) {

                                        // If directColor is enabled, set foreground colour
                                        // to specified colour
                                        if (res_directColor)
                                            setF(Pm_m[3], Pm_m[4], Pm_m[5]);

                                        // If not, set foreground colour to closest match in palette
                                        else
                                            setF(matchColor(Pm_m[3], Pm_m[4], Pm_m[5]));
                                    }
                                }

                                // ESC [ 38 ; 5 ; Ps m
                                // This code indicates to set the foreground colour to Ps, where
                                // Ps is an integer value between 0 and 255 that corresponds to
                                // one of the 256 colours in the xterm-256color palette.
                                if (Pm_m.length==3 && Pm_m[0]==38 && Pm_m[1]==5 && Pm_m[2]<256)
                                    setF(Pm_m[2]);

                                // ESC [ 48 ; 2 ; Pi ; Pr ; Pg ; Pb m
                                // This code indicates to set the background colour to the
                                // closest match in the 256 colour palette. Pr, Pg and Pb
                                // indicate the red, green and blue components of the colour,
                                // and Pi is ignored.
                                // All parameters must be integer values from 0 to 255.
                                // If directColor is enabled, this code instead sets the background
                                // colour to the specified colour rather than looking for the
                                // closest colour in the palette.
                                if (Pm_m.length==6 && Pm_m[0]==48 && Pm_m[1]==2) {

                                    // Check that R, G and B values are all below 256
                                    if (Pm_m[3]<256 && Pm_m[4]<256 && Pm_m[5]<256) {

                                        // If directColor is enabled, set background colour
                                        // to specified colour
                                        if (res_directColor)
                                            setB(Pm_m[3], Pm_m[4], Pm_m[5]);

                                        // If not, set background colour to closest match in palette
                                        else
                                            setB(matchColor(Pm_m[3], Pm_m[4], Pm_m[5]));
                                    }
                                }

                                // ESC [ 48 ; 5 ; Ps m
                                // This code indicates to set the background colour to Ps, where
                                // Ps is an integer value between 0 and 255 that corresponds to
                                // one of the 256 colours in the xterm-256color palette.
                                if (Pm_m.length==3 && Pm_m[0]==48 && Pm_m[1]==5 && Pm_m[2]<256)
                                    setB(Pm_m[2]);

                                // ESC [ 38 ; 2 ; Pr ; Pg ; Pb m
                                // This code indicates to set the foreground colour to the
                                // closest match in the 256 colour palette. Pr, Pg and Pb
                                // indicate the red, green and blue components of the colour.
                                // All parameters must be integer values from 0 to 255.
                                // If directColor is enabled, this code instead sets the foreground
                                // colour to the specified colour rather than looking for the
                                // closest colour in the palette.
                                if (Pm_m.length==5 && Pm_m[0]==38 && Pm_m[1]==2) {

                                    // Check that R, G and B values are all below 256
                                    if (Pm_m[2]<256 && Pm_m[3]<256 && Pm_m[4]<256) {

                                        // If directColor is enabled, set foreground colour
                                        // to specified colour
                                        if (res_directColor)
                                            setF(Pm_m[2], Pm_m[3], Pm_m[4]);

                                        // If not, set foreground colour to closest match in palette
                                        else
                                            setF(matchColor(Pm_m[2], Pm_m[3], Pm_m[4]));
                                    }
                                }

                                // ESC [ 48 ; 2 ; Pr ; Pg ; Pb m
                                // This code indicates to set the background colour to the
                                // closest match in the 256 colour palette. Pr, Pg and Pb
                                // indicate the red, green and blue components of the colour.
                                // All parameters must be integer values from 0 to 255.
                                // If directColor is enabled, this code instead sets the background
                                // colour to the specified colour rather than looking for the
                                // closest colour in the palette.
                                if (Pm_m.length==5 && Pm_m[0]==48 && Pm_m[1]==2) {

                                    // Check that R, G and B values are all below 256
                                    if (Pm_m[2]<256 && Pm_m[3]<256 && Pm_m[4]<256) {

                                        // If directColor is enabled, set background colour
                                        // to specified colour
                                        if (res_directColor)
                                            setB(Pm_m[2], Pm_m[3], Pm_m[4]);

                                        // If not, set background colour to closest match in palette
                                        else
                                            setB(matchColor(Pm_m[2], Pm_m[3], Pm_m[4]));
                                    }
                                }
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ Ps n   -   Device Status Report
                        // If the code ends in "n" and does not contain a "?",
                        // it means report device status. Specifically, if the parameter is 5,
                        // xtermView will print the escape code "ESC [ 0 n" which means "ok".
                        // If the parameter is 6,
                        // xtermView will instead print the escape code "ESC [ r ; c R",
                        // where r is current cursor row and c is current cursor column
                        // (one-based, 1 is first row, not 0).
                        // If the parameter is something other than
                        // 5 or 6 the escape code is ignored.
                        case 'n':
                            if (e.get(1)!='?') {
                                switch (parsePs(charLToA(e))) {
                                    // If parameter is 5, send "ESC [ 0 n"
                                    case 5:
                                        shell.output(new char[]{27,'[','0','n'}); // "ESC [ 0 n"
                                        break;

                                    // If parameter is 6, send "ESC [ r ; c R"
                                    // where r is cursor row and c is cursor column
                                    case 6:
                                        List<Character> L = new ArrayList<>();
                                        L.add((char)27);                                      // ESC
                                        L.add('[');                                           // [
                                        L.addAll(Arrays.asList(intToChars(curY+1)));          // r
                                        L.add(';');                                           // ;
                                        L.addAll(Arrays.asList(intToChars(curX+1)));          // c
                                        L.add('R');                                           // R
                                        shell.output(charLToA(L));
                                        break;
                                }
                            }

                            // ---------------------------------------------------------------------

                            // ESC [ Ps ; Ps r   -   Set Scrolling Region
                            // If the code ends in "r" and does not contain a "$" or "?",
                            // it means to set the scrolling region.
                            // Additionally, this code moves the cursor to the leftmost column of
                            // the first row of the scrolling region. The scrolling region is
                            // the area in the screen buffer that is edited during scrolling.
                            // The first parameter sets the top row in the scrolling region and
                            // the second parameter sets the bottom row.
                            // By default,
                            // the scrolling region is the full size of the screen buffer.
                            // If no parameters are given,
                            // the scrolling region is set to the full size of the screen buffer.
                            // Note that parameters are one-based (NOT zero-based),
                            // so 1 is the first row, not 0.
                        case 'r':
                            if (e.get(e.size()-1) != '$' && e.get(1) != '?') {
                                int Pm_r1[] = parsePm(charLToA(e));

                                // If exactly two parameters are detected,
                                // set scrolling region depending on parameters
                                if (Pm_r1.length == 2) {
                                    // Set starting row of scrolling region
                                    if (Pm_r1[0] < rows && Pm_r1[0] > 0) scrollStart = Pm_r1[0] - 1;
                                    else scrollStart = 0;

                                    // Set last row of scrolling region
                                    if (Pm_r1[1]<=rows && Pm_r1[1]>Pm_r1[0]) scrollEnd = Pm_r1[1]-1;
                                    else scrollEnd = rows - 1;
                                }

                                // If exactly zero parameters are detected, set scrolling region to
                                // full size of screen
                                if (Pm_r1.length == 0) {
                                    scrollStart = 0;
                                    scrollEnd = rows - 1;
                                }

                                // Move cursor to start of scrolling region
                                curX = 0;
                                curY = scrollStart;
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ s   -   Save Cursor (SCOSC, ANSI.SYS)
                        // If the code ends in "s" and contains no parameters,
                        // it means save the cursor position.
                        // The saved cursor position can be restored with "ESC [ u".
                        case 's':
                            if (e.size() == 2) {
                                ncurX = curX; // Save cursor X
                                ncurY = curY; // Save cursor Y
                            }
                            break;

                        // -------------------------------------------------------------------------

                        // ESC [ u   -   Restore Cursor (SCORC, ANSI.SYS)
                        // If the code ends in "u" and contains no parameters, it means restore the
                        // previously saved cursor position.
                        // The cursor position can be saved with "ESC [ s".
                        case 'u':
                            if (e.size() == 2) {
                                curX = ncurX<cols ? ncurX : cols-1; // Restore cursor X
                                curY = ncurY<rows ? ncurY : rows-1; // Restore cursor Y
                            }
                            break;

                        // -------------------------------------------------------------------------
                    }
                    e.clear();
                }

                // If the code begins with "]" and ends with BEL or ST,
                // it is an Operating System Command (OSC)
                else if (e.get(0)==']' && (e.get(e.size()-1)==7 ||
                        (e.get(e.size()-1)==27 && e.get(e.size()-1)==92))) {
                    isEscaping = false;
                    Log.d("ViewRoot_ESCAPECODE", String.valueOf(charLToA(e)));

                    // OSC codes are
                    // always in the form "ESC ] Ps ; Pt BEL" or sometimes "ESC ] Ps ; Pt ST".
                    // In 7-bit mode,
                    // BEL" (bell) is ASCII character 7, and "ST" (string terminator) is "ESC \".
                    String osc[] = parseOsc(charLToA(e));
                    switch (osc[0]) {

                        // ESC ] 0 ; Pt BEL
                        // ESC ] 1 ; Pt BEL
                        // ESC ] 2 ; Pt BEL
                        // These are interpreted as "set title".
                        // Pt is a text parameter consisting of one or more readable characters.
                        // The code causes the xterm window title to be set to Pt.
                        // If Pt is equal to "?" (a single question mark character), xtermView
                        // will instead send back this message:
                        // ESC ] Ps ; Pt BEL
                        // where Ps is 0, 1 or 2 (same as the received code), and Pt is the current
                        // window title.
                        case "0": case "1": case "2":
                            // If Pt is a single question mark, send the current window title
                            if (osc[1].equals("?")) {
                                List<Character> L = new ArrayList<>();
                                L.add((char)27);                                       // ESC
                                L.add(']');                                            // ]
                                L.add(osc[0].charAt(0));                               // Ps
                                L.add(';');                                            // ;
                                L.addAll(Arrays.asList(chars(osc[1].toCharArray())));  // Pt
                                L.add((char)7);                                        // BEL
                                shell.output(charLToA(L));
                            }

                            // If not, set title
                            else {
                                title = osc[1];
                                onTitleChange.run();
                            }
                    }

                    e.clear();
                }
            }
        }

        else { // If we are not looking for an escape code
            // If the character is a special non-readable character
            if ((int)c < 32 || (int)c == 127) switch ((byte) c) {
                // If we receive an escape character
                case 27:
                    isEscaping = true; // Start looking for escape codes
                    break;

                // If we receive a carriage return character
                case 13:
                    curX = 0; // Move cursor back to beginning of line
                    break;

                // If we receive a line feed, form feed or vertical tab character
                // (xterm treats VT and FF as LF)
                case 10:  // LF (line feed)
                case 11:  // VT (vertical tab)
                case 12:  // FF (form feed)
                    // Insert implicit carriage return if automatic newline mode is enabled
                    if (autoNewline) curY = 0;
                    // Move cursor to next line (without changing cursor column)
                    curY++;
                    shouldScroll = true;
                    break;

                // If we receive a backspace character
                // (backspace means move cursor back one space without deleting)
                case 8:
                    if (curX == 0) { // If we're at beginning of a line
                        // Move cursor to last column of previous row
                        curX = cols - 1;
                        curY--;
                        shouldScroll = true;

                    } else { // If we're not at the beginning of a line
                        curX--; // Move cursor back one space
                    }
                    break;

                // If we receive a bell character, run the onBell runnable
                case 7:
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onBell.run();
                        }
                    });
                    break;

                // If we receive an enquiry character, send back the answerbackString resource
                case 5:
                    shell.output(res_answerbackString.toCharArray());
            }

            // Render non-special characters normally
            else {

                // Decode character from DEC special graphics set if necessary
                char z = decSpecGraph ? decodeDecSpecGraph(c) : c;

                // If the cursor is beyond the scrolling region:
                if (curY == scrollEnd + 1 && shouldScroll) {
                    // Add the first row to the scrollback
                    if (!isAlt) {
                        Bitmap bitBuffer = bitCrop(scrollStart, scrollStart);
                        scrollback.add(0, bitBuffer);
                        if (scrollback.size() > Global.getInstance().maxScrollback)
                            scrollback.remove(scrollback.size()-1);
                    }

                    // Scroll up by one line
                    curY--;
                    scrollUp(1);
                }

                // If the cursor is still below the screen, move it to the last on-screen row
                if (curY >= rows && scrollEnd != rows) curY = rows - 1;


                screenBuffer[pos(curY, curX)] = z; // Log normal characters into screenBuffer

                // Log colours and typefaces of normal characters
                screenBufferF[pos(curY, curX)] = fpaint.getColor();
                screenBufferB[pos(curY, curX)] = bpaint.getColor();
                screenBufferT[pos(curY, curX)] = typeface;

                // Draw character to temporary text bitmap
                tcanvas.drawColor(screenBufferB[pos(curY,curX)]); // Draw text background
                tcanvas.drawText(new char[]{z}, 0, 1,
                        x(0)-paddingX, y(0)-paddingY, typeface>0 ? fpaintbold:fpaint);

                // Draw text bitmap to screen
                canvas.drawBitmap(tbitmap, paddingX+(charWidth*curX),
                        paddingY+(charHeight*curY), null);

                curX++; // Move cursor forwards
            }

            // If the cursor is beyond the scrolling region:
            if (curY == scrollEnd + 1 && shouldScroll) {
                // Add the first row to the scrollback
                if (!isAlt) {
                    Bitmap bitBuffer = bitCrop(scrollStart, scrollStart);
                    scrollback.add(0, bitBuffer);
                    if (scrollback.size() > Global.getInstance().maxScrollback)
                        scrollback.remove(scrollback.size()-1);
                }

                // Scroll up by one line
                curY--;
                scrollUp(1);
            }

            // If the cursor is still below the screen, move it to the last on-screen row
            if (curY >= rows && scrollEnd != rows) curY = rows - 1;
        }


        if (curX == cols) { // If the terminal row is full:
            // Put cursor at beginning of next line
            curX = 0;
            curY++;
        }
    }

    private synchronized void basicRender(char c) {
        screenBuffer[pos(curY, curX)] = c; // Log normal characters into screenBuffer

        // Log colours and typefaces of normal characters
        screenBufferF[pos(curY, curX)] = fpaint.getColor();
        screenBufferB[pos(curY, curX)] = bpaint.getColor();
        screenBufferT[pos(curY, curX)] = typeface;

        // Draw character to temporary text bitmap
        tcanvas.drawColor(screenBufferB[pos(curY,curX)]); // Draw text background
        tcanvas.drawText(new char[]{c}, 0, 1,
                x(0)-paddingX, y(0)-paddingY, typeface>0 ? fpaintbold:fpaint);

        // Draw text bitmap to screen
        canvas.drawBitmap(tbitmap, paddingX+(charWidth*curX),
                paddingY+(charHeight*curY), null);

        curX++; // Move cursor forwards
    }

    public interface callback { void output(char c[]); }






























    // Public methods

    /**
     * Add a set of characters to the terminal screen.
     * Special characters and escape codes will be interpreted accordingly.
     * @param chars Set of characters to add.
     */
    public synchronized void write(char chars[], int length) {
        // Only render characters if the terminal has been initialised
        if (ready) {
            for (int i = 0; i < length; i++) {
                Log.d("ViewRoot_INTERCEPTED", String.valueOf((int) chars[i]) + "     " +
                        String.valueOf(chars[i]));
                render(chars[i]); // Render character
            }

            postInvalidate(); // Triggers a re-rendering of the view
        }

        // If the terminal is not ready to render characters, store them until ready
        else
            for (int i = 0; i < length; i++) {
                preBuffer.add(chars[i]);
            }
    }

    /**
     * Set the action to run when a bell character is received by the terminal.
     * @param R Runnable to run when bell character is received.
     */
    public synchronized void setOnBellListener(Runnable R) { onBell = R; }

    /**
     * Set actions to run when various touch events occur (view is tapped, view is scrolled, etc...)
     * These will be run after the View's own events occur.
     * Do not add an onClick listener to this view. Use this method instead.
     * @param L GestureDetector listener to use for this View.
     */
    public synchronized void setGestureListener(GestureDetector.OnGestureListener L){
        gesture = L;
    }

    /**
     * Set the action to run when xtermView wants to send data to
     * whatever the application is communicating with.
     * @param C callback object to run.
     */
    public synchronized void setCallback(callback C) { shell = C; }

    /**
     * Returns true if keypad type is Application Keypad,
     * returns false if keypad type is Normal Keypad.
     * The escape codes to send when keys are pressed are different for
     * "Application Mode" and "Normal Mode".
     * The keypad type affects the numbers 0-9 on the keypad as well as
     * + - * / = . , from the keypad and enter from the keypad.
     * Check here for more details:
     * <pre>https://www.gnu.org/software/screen/manual/screen.html#Input-Translation</pre>
     * If you are connected to a UNIX shell check your keypad type frequently.
     * <pre></pre>
     */
    public synchronized boolean isApplicationKeypad() { return appKeypad; }

    /**
     * Returns true if cursor keys type is Application Cursor Keys,
     * returns false if cursor keys type is Normal Cursor Keys.
     * The escape codes to send when keys are pressed are different for
     * "Application Mode" and "Normal Mode".
     * The cursor keys type affects how the cursor keys (arrow keys) on the keyboard are processed.
     * Check here for more details:
     * <pre>https://www.gnu.org/software/screen/manual/screen.html#Input-Translation</pre>
     * If you are connected to a UNIX shell check your keypad type frequently.
     * <pre></pre>
     */
    public synchronized boolean isApplicationCursorKeys() { return appCursorKeys; }

    /**
     * Returns true if current screen buffer is Alternate Screen Buffer,
     * returns false if current screen buffer is Normal Screen Buffer.
     * <pre></pre>
     */
    public synchronized boolean isAlternateScreenBuffer() { return isAlt; }

    /**
     * Returns true if cursor is shown, returns false if cursor is hidden.
     * <pre></pre>
     */
    public synchronized boolean isCursorShown() { return cursorShown; }

    /**
     * Returns true if terminal is using DEC Special Graphics,
     * returns false if terminal is using plain UTF-8.
     * If DEC Special Graphics is enabled,
     * the characters 95 to 126 (decimal, inclusive) will be replaced with
     * the character with the same charcode
     * from the DEC special graphics set. The DEC special graphics set can be seen here:
     * <pre>https://en.wikipedia.org/wiki/DEC_Special_Graphics</pre>
     * Also look at this website:
     * <pre>https://vt100.net/docs/vt100-ug/table3-9.html</pre>
     */
    public synchronized boolean isDecSpecGraph() { return decSpecGraph; }

    /**
     * Gets the current mouse tracking mode.
     * <p>
     *     0 = Mouse Tracking off.
     * </p><p>
     *     1 = X10 Compatibility Mode.
     *     Only tracks left mouse button clicks.
     *     Tracks mouse co-ordinates only when the left mouse button is clicked.
     *     xtermView emulates this by reporting screen taps (down events only) as
     *     left mouse button clicks.
     * </p><p>
     *     2 = Normal Mouse Tracking.
     *     Tracks all mouse button clicks and mouse button releases.
     *     Tracks mouse co-ordinates and modifier keys (shift, control and meta)
     *     only when pressing or releasing any mouse button.
     *     xtermView emulates this by reporting screen taps (down and up events)
     *     as left mouse button clicks and releases.
     * </p><p>
     *     3 = Button-Event Tracking.
     *     Same as Normal Mouse Tracking, except it
     *     tracks mouse co-ordinates and modifier keys as long as a mouse button is held down,
     *     rather than only when pressing or releasing.
     *     xtermView emulates this by additionally reporting the
     *     user dragging their finger on the screen.
     * </p><p>
     *     4 = Any-Event Tracking.
     *     Same as Button-Event Tracking, except it
     *     tracks mouse co-ordinates and modifier keys at all times.
     * </p>
     * @return Mouse Tracking mode, an integer from 0-4 inclusive.
     */
    public synchronized int getMouseTrackingMode() {
        if (mouseTracking_any) return 4;
        else if (mouseTracking_button) return 3;
        else if (mouseTracking_normal) return 2;
        else if (mouseTracking_x10compat) return 1;
        else return 0;
    }

    /**
     * Returns true if Highlight Tracking is enabled,
     * returns false if Highlight Tracking is disabled.
     * Highlight Tracking allows UNIX applications to track text being highlighted (selected)
     * in the terminal.
     * <pre></pre>
     */
    public synchronized boolean isHighlightTracking() { return mouseTracking_highlight; }

    /**
     * Set the action to run when keypad changes from "Normal Keypad" to "Application Keypad",
     * or vice versa.
     * You can check which keypad is active using isApplicationKeypad().
     * @param R Runnable to run when keypad changes.
     */
    public synchronized void setOnKeypadChangeListener(Runnable R) { onKeypadChange = R; }

    /**
     * Set the action to run when cursor keys change from "Normal Cursor Keys"
     * to "Application Cursor Keys", or vice versa.
     * You can check which keypad is active using isApplicationCursorKeys().
     * @param R Runnable to run when cursor keys type changes.
     */
    public synchronized void setOnCursorKeysChangeListener(Runnable R) { onCursorKeysChange = R; }

    /**
     * Set the action to run when screen buffer changes from "Normal Screen Buffer" to
     * "Alternate Screen Buffer", or vice versa.
     * You can check which screen buffer is active with isAlternateScreenBuffer().
     * @param R Runnable to run when screen buffer changes.
     */
    public synchronized void setOnScreenBufferChangeListener(Runnable R) {onScreenBufferChange = R;}

    /**
     * Set the action to run when cursor is shown or hidden.
     * You can check if cursor is shown or hidden with isCursorShown().
     * @param R Runnable to run when cursor is shown or hidden.
     */
    public synchronized void setOnCursorShownHiddenListener(Runnable R) { onCursorShownHidden = R; }

    /**
     * Set the action to run when the DEC special graphics set is enabled or disabled.
     * You can check if the Dec special graphics set is enabled or disabled with isDecSpecGraph().
     * @param R Runnable to run when the DEC special graphics set is enabled or disabled.
     */
    public synchronized void setOnDecSpecGraphListener(Runnable R) { onDecSpecGraph = R; }

    /**
     * Set the action to run when the Mouse Tracking mode changes.
     * This does not track changes in Highlight Tracking.
     * You can get the current Mouse Tracking mode using getMouseTrackingMode().
     * @param R Runnable to run when the Mouse Tracking mode changes.
     */
    public synchronized void setOnMouseTrackingChangeListener(Runnable R) {onMouseTrackingChange=R;}

    /**
     * Set the action to run when Highlight Tracking is enabled or disabled.
     * You can check if Highlight Tracking is enabled or disabled with isHighlightTracking().
     * @param R Runnable to run when Highlight Tracking is enabled or disabled.
     */
    public synchronized void
    setOnHighlightTrackingChangeListener(Runnable R) { onHighlightTrackingChange = R; }

    /**
     * Set the action to run when the xterm window title changes, either because you used setTitle()
     * or because an escape code was sent that caused the title to change.
     * You can get the new title using getTitle().
     * @param R Runnable to run when title changes.
     */
    public synchronized void setOnTitleChangeListener(Runnable R) { onTitleChange = R; }

    /**
     * Set whether or not the control key is currently being held down.
     * This is required for correct performance of Mouse Tracking.
     * @param k State of control key.
     */
    public synchronized void setIsCtrlDown(boolean k) { ctrlDown = k; }

    /**
     * Set whether or not the shift key is currently being held down.
     * This is required for correct performance of Mouse Tracking.
     * @param k State of shift key.
     */
    public synchronized void setIsShiftDown(boolean k) { shiftDown = k; }

    /**
     * Set whether or not the meta key is currently being held down.
     * This is required for correct performance of Mouse Tracking.
     * Most keyboards do not have a meta key, so use the alt key instead if that is the case.
     * @param k State of meta (or alt) key.
     */
    public synchronized void setIsMetaDown(boolean k) { metaDown = k; }

    /**
     * Sets xterm-256color terminal colours (0 to 15).
     * @param id ID (xterm number) of colour to set. Must be between 0 and 256.
     *           Consult this website for the full list of colours
     *           (xtermview uses non-standard colours for
     *           colours 0 to 15 by default that differ from those on the website):
     *           <pre>https://jonasjacek.github.io/colors/</pre>
     * @param color Colour to change it to. Must be an integer more than 0.
     *              For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     * @return False if id or color was invalid, otherwise true.
     */
    public synchronized boolean setColor(int id, int color) {
        if (id >= 0 && id < 256 && color >= 0) {
            colors[id + 3] = color;
            return true;
        }
        return false;
    }

    /**
     * Gets one of the terminal colours as an integer.
     * @param id ID (xterm number) of colour to get. Must be between 0 and 256.
     *           Consult this website for the full list of colours
     *           (xtermview uses non-standard colours for
     *           colours 0 to 15 by default that differ from those on the website):
     *           <pre>https://jonasjacek.github.io/colors/</pre>
     * @return Colour as an integer.
     *         For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     *         Returns -1 instead if id is invalid.
     */
    public synchronized int getColor(int id) {
        if (id >= 0 && id < 256)
            return colors[id+3];
        return -1;
    }

    /**
     * Set default foreground (text) colour to use in the terminal.
     * Default value is 0xFFD3D7CF.
     * @param color Colour to change it to. Must be an integer more than 0.
     *              For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized boolean setDefaultForegroundColor(int color) {
        if (color >= 0) {
            colors[0] = color;
            return true;
        }
        return false;
    }

    /**
     * Gets the default foreground (text) colour to use in the terminal.
     * Default value is 0xFFD3D7CF.
     * @return Colour as an integer.
     *         For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized int getDefaultForegroundColor() { return colors[0]; }

    /**
     * Set default background (highlight) colour to use in the terminal.
     * Default value is 0xFF000000.
     * @param color Colour to change it to. Must be an integer more than 0.
     *              For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized boolean setDefaultBackgroundColor(int color) {
        if (color >= 0) {
            colors[1] = color;
            return true;
        }
        return false;
    }

    /**
     * Gets the default background (highlight) colour to use in the terminal.
     * Default value is 0xFF000000.
     * @return Colour as an integer.
     *         For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized int getDefaultBackgroundColor() { return colors[1]; }

    /**
     * Set default cursor colour to use in the terminal. Default value is 0xFF00FF00.
     * @param color Colour to change it to. Must be an integer more than 0.
     *              For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized boolean setDefaultCursorColor(int color) {
        if (color >= 0) {
            colors[2] = color;
            return true;
        }
        return false;
    }

    /**
     * Gets the default cursor colour to use in the terminal. Default value is 0xFF00FF00.
     * @return Colour as an integer.
     *         For example, green is 0xFF00FF00, and red is 0xFFFF0000, and blue is 0xFF0000FF.
     */
    public synchronized int getDefaultCursorColor() { return colors[2]; }

    /**
     * Set the title of the xterm window. You should set this to the name of your application.
     * Some Linux programs running in a shell may cause the title to change automatically.
     * This change may be detected using setOnTitleChangeListener().
     * @param s Title.
     */
    public synchronized void setTitle(String s) { title = s; }

    /**
     * Set the title of the xterm window. You can set the title using setTitle().
     * Some Linux programs running in a shell may cause the title to change automatically.
     * This change may be detected using setOnTitleChangeListener().
     */
    public synchronized String getTitle() { return title; }

    /**
     * Gets the transcript, which is a List of Characters containing all characters ever printed
     * by the terminal.
     * @param escape Whether or not to include characters that were part of escape codes.
     * @param special Whether or not to include special non-readable characters.
     * @return Transcript.
     */
    public synchronized List<Character> getTranscript(boolean escape, boolean special) {
        if (escape) {
            if (special) return transcript1;
            else return transcript2;
        } else {
            if (special) return transcript3;
            else return transcript4;
        }
    }

    /**
     * Clears the transcript.
     */
    public synchronized void clearTranscript() {
        transcript1.clear();
        transcript2.clear();
        transcript3.clear();
        transcript4.clear();
    }

    /**
     * Sets xterm's answerbackString resource.
     * Upon receiving a ^E (CTRL+E) character, the terminal sends back a string.
     * By default this string is an empty string,
     * however many terminal emulators choose to send back the name of the terminal emulator,
     * for example PuTTY.
     * @param s String to set answerback string to.
     */
    public synchronized void setRes_answerbackString(String s) { res_answerbackString = s; }

    /**
     * Gets xterm's answerbackString resource.
     * Upon receiving a ^E (CTRL+E) character, the terminal sends back a string.
     * By default this string is an empty string,
     * however many terminal emulators choose to send back the name of the terminal emulator,
     * for example PuTTY.
     * @return answerbackString.
     */
    public synchronized String getRes_answerbackString() { return res_answerbackString; }

    /**
     * Enables or disables xterm's directColor resource.
     * If this is enabled, shells can make xtermView print in any 8-bit colour, rather than only
     * those in the 256 colour palette.
     * This is enabled by default.
     * @param b Enable or disable directColor.
     */
    public synchronized void setRes_directColor(boolean b) { res_directColor = b; }

    /**
     * Gets the state of xterm's directColor resource.
     * If this is enabled, shells can make xtermView print in any 8-bit colour, rather than only
     * those in the 256 colour palette.
     * This is enabled by default.
     * @return State of directColor.
     */
    public synchronized boolean getRes_directColor() { return res_directColor; }

    /**
     * Enables or disables xterm's titeInhibit resource.
     * Leave this disabled unless you have a really good reason to enable it.
     * Since we do not use termcap,
     * enabling this essentially prevents using the 47, 1047, 1048 and 1049 private mode controls,
     * which are associated with the Alternate Screen Buffer.
     * @param b Enable or disable titeInhibit.
     */
    public synchronized void setRes_titeInhibit(boolean b) { res_titeInhibit = b; }

    /**
     * Gets the state of xterm's titeInhibit resource.
     * Leave this disabled unless you have a really good reason to enable it.
     * Since we do not use termcap,
     * enabling this essentially prevents using the 47, 1047, 1048 and 1049 private mode controls,
     * which are associated with the Alternate Screen Buffer.
     * @return State of titeInhibit.
     */
    public synchronized boolean getRes_titeInhibit() { return res_titeInhibit; }



























    // Private methods

    /**
     * Used by render() to disable checking for escape codes after receiving one.
     * Also can log received escape codes.
     */
    private synchronized void logEscape() {
        isEscaping = false;
        Log.d("ViewRoot_ESCAPECODE", String.valueOf(charLToA(e)));
        e.clear();
    }

    /**
     * Routine to collect mouse information and output it.
     * @param mode Integer from 0-4 denoting mouse tracking mode
     * @param screenX X co-ordinate of mouse relative to screen.
     * @param screenY Y co-ordinate of mouse relative to screen.
     * @param down Whether or not mouse is down (i.e. is the user currently touching the screen).
     * @param ctrl Whether or not control key is pressed.
     * @param shift Whether or not shift key is pressed.
     * @param meta Whether or not meta key is pressed.
     * @param diff Whether or not a mouse button is being held down AND
     *             the mouse is in a different location than it was when
     *             the button was first pressed.
     */
    private synchronized void doMouseTracking(int mode,
                                              float screenX,
                                              float screenY,
                                              boolean down,
                                              boolean ctrl,
                                              boolean shift,
                                              boolean meta,
                                              boolean diff) {
        mouseX = screenX;
        mouseY = screenY;

        float viewX = screenX - getLeft() - paddingX;
        float viewY = screenY - getTop() - paddingY;

        // Column the user tapped (one-based)
        int cellX = Math.min(cols - 1, (int) (viewX / charWidth)) + 1;
        // Row the user tapped (one-based)
        int cellY = Math.min(cols - 1, (int) (viewY / charHeight)) + 1;

        // Clip cell values above last row/column number and above 223
        int mRows = Math.min(223, rows + 1);
        int mCols = Math.min(223, cols + 1);
        cellX = Math.min(mCols, cellX);
        cellY = Math.min(mRows, cellY);

        switch (mode) {
            // X10 Compatibility Mouse Tracking
            // Output in the form   "ESC [ M SP Cx Cy"
            // where Cx is column and Cy is row, and SP is the space character.
            // Outputs only when the left mouse button is pressed, not when it is released.

            // As usual, the row and column numbers are one-based,
            // so row 1 is the first row, not row 0.
            // The row and column are encoded in ASCII rather than in digits,
            // where each character has charcode (value + 32).
            // For example, ! (exclamation point) means 1,
            // " (quotation mark) means 2, # (number sign) means 3, and so on.
            // Because the characters are limited to 1 byte,
            // the maximum row/column value is 223 (255-32).
            // That shouldn't matter unless you're using an unusually large terminal screen...
            case 1:
                mouseCX = cellX;
                mouseCY = cellY;
                shell.output(new char[]{
                        27,                    // ESC
                        91,                    // [
                        77,                    // M
                        32,                    // SP
                        (char) (mouseCX + 32), // Cx
                        (char) (mouseCY + 32)  // Cy
                });
                break;



            // Normal Mouse Tracking
            // Output in the form   "ESC [ M Cb Cx Cy"
            // where Cx is column and Cy is row, and Cb will be explained later.
            // Outputs only when any mouse button is pressed or released.

            // As usual, the row and column numbers are one-based,
            // so row 1 is the first row, not row 0.
            // The row and column are encoded in ASCII rather than in digits,
            // where each character has charcode (value + 32).
            // For example, ! (exclamation point) means 1,
            // " (quotation mark) means 2, # (number sign) means 3, and so on.
            // Because the characters are limited to 1 byte,
            // the maximum row/column value is 223 (255-32).
            // That shouldn't matter unless you're using an unusually large terminal screen...

            // To calculate the value of Cb, first start with an integer equal to 32 if
            // the left mouse button is pressed, 33 if the middle mouse button is pressed,
            // 34 if the right mouse button is pressed or 35 if no mouse buttons are pressed.
            // If multiple mouse buttons are pressed,
            // just choose the latest one that was pressed. If shift is pressed, add 4.
            // If meta (or alt) is pressed, add 8. If control is pressed, add 16.
            // Now Cb is the character with the same ASCII character code as your new integer.
            case 2:
                mouseCX = cellX;
                mouseCY = cellY;
                int Cb2 = down ? 32 : 35; // 32 if LMB is pressed, 35 if not
                Cb2 += shift ? 4 : 0;     // Add 4 if shift is down
                Cb2 += meta ? 8 : 0;      // Add 8 if meta is down
                Cb2 += ctrl ? 16 : 0;     // Add 16 if ctrl is down
                shell.output(new char[]{
                        27,                    // ESC
                        91,                    // [
                        77,                    // M
                        (char) Cb2,            // Cb
                        (char) (mouseCX + 32), // Cx
                        (char) (mouseCY + 32)  // Cy
                });
                break;



            // Button-Event Mouse Tracking / Any-Event Mouse Tracking
            // If a mouse button has just been pressed or released,
            // outputs in the same format as in Normal Mouse Tracking.
            // If a mouse button is being held down and the mouse has moved to a different cell
            // Outputs WHILE any mouse button is pressed,
            // also outputs ONCE when a mouse button is released.
            // In the case of Any-Event Mouse Tracking,
            // it also outputs even if no mouse button is held, as long as mouse has moved.

            // If the mouse button is being held down and the mouse has moved to a different cell,
            // a packet is sent in the format of Normal Mouse Tracking,
            // except 32 is added to the Cb integer before sending.
            case 3:
            case 4:
                // Send packet for mouse being held down only if
                // mouse has moved or a modifier key has changed state
                if (!diff || !down || cellX!=mouseCX || cellY!=mouseCY || mCtrlDown!=ctrl ||
                        mShiftDown!=shift || mMetaDown!=meta) {
                    mouseCX = cellX;
                    mouseCY = cellY;
                    int Cb3 = down ? 32 : 35; // 32 if LMB is pressed, 35 if not
                    Cb3 += shift ? 4 : 0;     // Add 4 if shift is down
                    Cb3 += meta ? 8 : 0;      // Add 8 if meta is down
                    Cb3 += ctrl ? 16 : 0;     // Add 16 if ctrl is down
                    Cb3 += diff ? 32 : 0;     // Add 32 if mouse button is held down and mouse moved
                    shell.output(new char[]{
                            27,                    // ESC
                            91,                    // [
                            77,                    // M
                            (char) Cb3,            // Cb
                            (char) (mouseCX + 32), // Cx
                            (char) (mouseCY + 32)  // Cy
                    });
                }
                break;
        }
        mCtrlDown = ctrl;
        mShiftDown = shift;
        mMetaDown = meta;
    }

    /**
     * Used by render() to scroll the scrolling region up when necessary.
     * @param L Number of lines to scroll by. Must be more than zero.
     */
    private synchronized void scrollUp(int L) {
        // If the number of lines to scroll is greater or equal to the number of rows,
        // just clear the scrolling region
        if (L > scrollEnd - scrollStart) {
            bClear(scrollStart, 0, (scrollEnd-scrollStart+1)*cols);
            canvas.drawColor(colors[1]);
        }

        // If not, actually perform the scrolling operation
        else {
            // Shift all but the first L rows upwards by one row
            bShift(scrollStart + L, 0, (scrollEnd-scrollStart-L+1) * cols,
                    (-cols)*L);

            // Clear the last L rows because they're now duplicates
            bClear(scrollEnd-L+1, 0, cols*L);

            // Crop the area between Lth row and last row
            Bitmap bitBuffer = bitCrop(scrollStart + L, scrollEnd);
            // Shift area upwards by L rows
            canvas.drawBitmap(bitBuffer, 0, paddingY + charHeight * scrollStart,
                    null);
            // Clear last L rows
            canvas.drawRect(0, paddingY+charHeight*(scrollEnd-L+1), getWidth(),
                    paddingY+charHeight*(scrollEnd+1), dpaint);
        }

        // Afterwards redraw the view
        postInvalidate();
    }

    /**
     * Used by render() to scroll the scrolling region down when necessary.
     * @param L Number of lines to scroll by. Must be more than zero.
     */
    private synchronized void scrollDown(int L) {
        // If the number of lines to scroll is greater or equal to the number of rows,
        // just clear the scrolling region
        if (L > scrollEnd - scrollStart) {
            bClear(scrollStart, 0, (scrollEnd-scrollStart+1)*cols);
            canvas.drawColor(colors[1]);
        }

        // If not, actually perform the scrolling operation
        else {
            // Shift all but the last L rows downwards by one row
            bShift(scrollStart, 0, (scrollEnd-scrollStart-L+1)*cols, cols*L);

            // Clear the first L rows because they're now duplicates
            bClear(scrollStart, 0, cols*L);

            // Crop the area between first row and Lth row from bottom
            Bitmap bitBuffer = bitCrop(scrollStart, scrollEnd-L);
            // Shift area down by L rows
            canvas.drawBitmap(bitBuffer, 0, paddingY + charHeight * (scrollStart+L),
                    null);
            // Clear first L rows
            canvas.drawRect(0, paddingY+charHeight*(scrollStart), getWidth(),
                    paddingY+charHeight*(scrollStart+L), dpaint);
        }

        // Afterwards redraw the view
        postInvalidate();
    }

    /**
     * Used by onScroll() and onFling() to scroll the entire screen up when necessary.
     * @param L Number of lines to scroll by. Must be more than zero.
     */
    private synchronized void scrollScreenUp(int L) {
        Bitmap bitBuffer = bitCrop(L, rows-1); // Crop the area between Lth row and last row
        canvas.drawBitmap(bitBuffer, 0, paddingY, null); // Shift area upwards by L rows
        // Clear last L rows
        canvas.drawRect(0, paddingY+charHeight*(rows-L), getWidth(),
                paddingY+charHeight*rows, dpaint);

        // Add rows to the bottom of the screen from scrollback or screen buffer
        for (int i = 0; i < L; i++)
            // If the row to add to the bottom of screen is part of screen buffer
            if (scrollLines-L-rows+i < 0) {
                bitBuffer = bitCropS(L-scrollLines+rows-i-1);
                canvas.drawBitmap(bitBuffer, 0, paddingY+charHeight*(rows-i-1),
                        null);
                // If the row to add to the bottom of screen is not part of screen buffer
                // (i.e. part of scrollback)
            } else
                canvas.drawBitmap(scrollback.get(scrollLines-L-rows+i), 0,
                        paddingY+charHeight*(rows-i-1), null);

        // Afterwards redraw the view
        postInvalidate();
    }

    /**
     * Used by onScroll() and onFling() to scroll the entire screen up when necessary.
     * @param L Number of lines to scroll by. Must be more than zero.
     */
    private synchronized void scrollScreenDown(int L) {
        // Crop the area between first row and Lth row from bottom
        Bitmap bitBuffer = bitCrop(0, rows-L-1);
        // Shift area down by L rows
        canvas.drawBitmap(bitBuffer, 0, paddingY + charHeight * L, null);
        // Clear first L rows
        canvas.drawRect(0, paddingY, getWidth(),
                paddingY+charHeight*L, dpaint);

        for (int i = 0; i < L; i++) // Add rows to the top of the screen from the scrollback
            canvas.drawBitmap(scrollback.get(scrollLines+L-i-1), 0,
                    paddingY+charHeight*i, null);

        // Afterwards redraw the view
        postInvalidate();
    }

    /**
     * Used by render() to redraw an entire row when required
     * @param row Row to redraw
     */
    private synchronized void redrawRow(int row) {
        // Save cursor position
        int _curX = curX;
        int _curY = curY;

        // Save colours
        int _fcolor = fpaint.getColor();
        int _bcolor = bpaint.getColor();

        // Save typeface
        byte _typeface = typeface;

        // Save text formatting
        boolean _isReversed = isReversed;

        // Move cursor to start of specified row
        curX = 0;
        curY = row;

        // Render characters in the specified row
        isReversed = false;
        for (char c : getBufferRow(row)) {
            // Restore the foreground colour for the character
            fpaint.setColor(screenBufferF[pos(curY, curX)]);
            fpaintbold.setColor(screenBufferF[pos(curY, curX)]);

            // Restore the background colour for the character
            bpaint.setColor(screenBufferB[pos(curY, curX)]);

            // Restore the typeface for the character
            typeface = screenBufferT[pos(curY, curX)];

            basicRender(c); // Render character with correct colours and typefaces
        }

        // Reset cursor position to what it was before
        curX = _curX;
        curY = _curY;

        // Reset colours
        fpaint.setColor(_fcolor);
        fpaintbold.setColor(_fcolor);
        bpaint.setColor(_bcolor);

        // Reset typeface
        typeface = _typeface;

        // Reset text formatting
        isReversed = _isReversed;

        postInvalidate(); // Triggers a re-drawing of the View
    }

    /**
     * Used by render() to restore the current screen buffer from memory
     */
    private synchronized void restoreBuffer() {
        canvas.drawColor(colors[1]);
        if (isAlt) canvas.drawBitmap(abitmap, 0, 0, null);
        else canvas.drawBitmap(sbitmap, 0, 0, null);
    }

    /**
     * Set foreground colour to specified colour ID
     * (0 is black, 1 is red, 2 is green, 3 is yellow, etc...)
     * @param id Colour ID, set to -3 to use default colour
     */
    private synchronized void setF(int id) {
        if (!isReversed) {
            fpaint.setColor(colors[id + 3]);
            fpaintbold.setColor(colors[id + 3]);
        } else
            bpaint.setColor(colors[id + 3]);
    }

    /**
     * Set foreground colour to specified RGB value
     * @param r Red component. Must be between 0 and 255.
     * @param g Green component. Must be between 0 and 255.
     * @param b Blue component. Must be between 0 and 255.
     */
    private synchronized void setF(int r, int g, int b) {
        if (r>=0 && r<256 && g>=0 && g<256 && b>=0 && b<256) {
            if (!isReversed) {
                fpaint.setColor(Color.argb(255, r, g, b));
                fpaintbold.setColor(Color.argb(255, r, g, b));
            } else
                bpaint.setColor(Color.argb(255, r, g, b));
        }
    }

    /**
     * Set background colour to specified colour ID
     * (0 is black, 1 is red, 2 is green, 3 is yellow, etc...)
     * @param i Colour ID, set to -2 to use default colour
     */
    private synchronized void setB(int i) {
        if (!isReversed)
            bpaint.setColor(colors[i + 3]);
        else {
            fpaint.setColor(colors[i + 3]);
            fpaintbold.setColor(colors[i + 3]);
        }
    }

    /**
     * Set background colour to specified RGB value
     * @param r Red component. Must be between 0 and 255.
     * @param g Green component. Must be between 0 and 255.
     * @param b Blue component. Must be between 0 and 255.
     */
    private synchronized void setB(int r, int g, int b) {
        if (r>=0 && r<256 && g>=0 && g<256 && b>=0 && b<256) {
            if (!isReversed)
                bpaint.setColor(Color.argb(255, r, g, b));
            else {
                fpaint.setColor(Color.argb(255, r, g, b));
                fpaintbold.setColor(Color.argb(255, r, g, b));
            }
        }
    }

    /**
     * Used by render() to parse parameters in escape codes with the escape character removed,
     * such as "[1P"
     * (in this case, the parameter would be the "1")
     * @param c Escape code
     * @return The parameter, in integer form
     */
    private synchronized int parsePs(char c[]) { return parsePm(c).length>0 ? parsePm(c)[0] : -1; }

    /**
     * Used by render() to parse parameters in escape codes with the escape character removed,
     * such as "[01;31m"
     * (in this case, the parameters would be 1 and 31)
     * @param c Escape code
     * @return List of parameters in integer form
     */
    private synchronized int[] parsePm(char c[]) {
        if (c.length < 3) return new int[0];
        List<Character> L = new ArrayList<>();
        List<Integer> R = new ArrayList<>();
        for (char x : c) {
            if (isNum(x)) L.add(x);
            if ((x == ';' || (byte)x > 63) && x != '[' && x != '?' && x != '>') {
                R.add(toNum(charLToA(L)));
                L.clear();
            }
        }
        return intLToA(R);
    }

    /**
     * Same as parsePm(), but allows the parameters to be separated by colons instead of semicolons.
     * However after the first colon is detected, colons must be used instead of semicolons.
     * This special behaviour is only used for the ISO-8613-6 controls (the ones allowing you to
     * use the colours 17 to 255) in the "ESC [ Pm m" escape code.
     * @param c Escape code
     * @return List of parameters in integer form
     */
    private synchronized int[] parsePmC(char c[]) {
        if (c.length < 3) return new int[0];
        boolean colon = false;
        List<Character> L = new ArrayList<>();
        List<Integer> R = new ArrayList<>();
        for (char x : c) {
            if (isNum(x)) L.add(x);
            if (((colon&&x==':') || (!colon&&(x==';'||x==':')) || (byte)x > 63)
                    && x != '[' && x != '?' && x != '>') {
                if (x==':') colon = true;
                R.add(toNum(charLToA(L)));
                L = new ArrayList<>();
            }
        }
        return intLToA(R);
    }

    private synchronized String[] parseOsc(char c[]) {
        List<Character> L = new ArrayList<>();
        String R[] = {"",""};
        boolean f = false;
        for (char x : c) {
            if (x != ']' || f) {
                if (x == ';' || x == 27 || x == 7) {
                    if (R[0].equals(""))
                        R[0] = String.valueOf(charLToA(L));
                    else {
                        R[1] = String.valueOf(charLToA(L));
                        return R;
                    }
                    L.clear();
                } else
                    L.add(x);
            } else f = true;
        }
        return R;
    }

    /**
     * Returns the id of the closest colour in the 256 colour palette
     * @param r Red component. Value between 0 and 255.
     * @param g Green component. Value between 0 and 255.
     * @param b Blue component. Value between 0 and 255.
     * @return xterm colour ID of the closest colour to the one specified.
     */
    private synchronized int matchColor(int r, int g, int b) {
        if (r>=0 && r<256 && g>=0 && g<256 && b>=0 && b<256) {
            int c = 16;

            if (r>47) c+=36;
            if (r>57) c+=36;
            if (r>77) c+=36;
            if (r>97) c+=36;
            if (r>117) c+=36;

            if (g>47) c+=6;
            if (g>57) c+=6;
            if (g>77) c+=6;
            if (g>97) c+=6;
            if (g>117) c+=6;

            if (b>47) c++;
            if (b>57) c++;
            if (b>77) c++;
            if (b>97) c++;
            if (b>117) c++;

            return c;
        }

        else return -1;
    }

    /**
     * Converts characters from UTF-8 to combined UTF-8 and DEC special graphics.
     * The characters 95 to 126 (decimal, inclusive) will be replaced with the
     * character with the same charcode
     * from the DEC special graphics set. The DEC special graphics set can be seen here:
     * <pre>https://en.wikipedia.org/wiki/DEC_Special_Graphics</pre>
     * Also look at this website:
     * <pre>https://vt100.net/docs/vt100-ug/table3-9.html</pre>
     * @param c Character to convert from UTF-8.
     * @return Converted character in combined UTF-8 and DEC special graphics.
     */
    private synchronized char decodeDecSpecGraph(char c) {
        switch ((int)c) {              //           UNICODE  |  DEC
            case 95: return '\u00A0';  // _   (underscore)   |  non-breaking space
            case 96: return '\u25C6';  // ` (grave accent)   |  diamond
            case 97: return '\u2592';  //                 a  |  medium shade (error indicator)
            case 98: return '\u2409';  //                 b  |  horizontal tab symbol    "HT"
            case 99: return '\u240C';  //                 c  |  form feed symbol         "FF"
            case 100: return '\u240D'; //                 d  |  carriage return symbol   "CR"
            case 101: return '\u240A'; //                 e  |  line feed symbol         "LF"
            case 102: return '\u00B0'; //                 f  |  ° (degree sign)
            case 103: return '\u00B1'; //                 g  |  ± (plus-minus sign)
            case 104: return '\u2424'; //                 h  |  newline symbol           "NL"
            case 105: return '\u240B'; //                 i  |  vertical tab symbol      "VT"
            case 106: return '\u2518'; //                 j  |  box drawing lower-right corner
            case 107: return '\u2510'; //                 k  |  box drawing upper-right corner
            case 108: return '\u250C'; //                 l  |  box drawing upper-left corner
            case 109: return '\u2514'; //                 m  |  box drawing lower-left corner
            case 110: return '\u253C'; //                 n  |  box drawing cross
            case 111: return '\u23BA'; //                 o  |  box drawing horizontal scan line 1
            case 112: return '\u23BB'; //                 p  |  box drawing horizontal scan line 3
            case 113: return '\u2500'; //                 q  |  box drawing horizontal line
            case 114: return '\u23BC'; //                 r  |  box drawing horizontal scan line 7
            case 115: return '\u23BD'; //                 s  |  box drawing horizontal scan line 9
            case 116: return '\u251C'; //                 t  |  box drawing left "T"
            case 117: return '\u2524'; //                 u  |  box drawing right "T"
            case 118: return '\u2534'; //                 v  |  box drawing bottom "T"
            case 119: return '\u252C'; //                 w  |  box drawing top "T"
            case 120: return '\u2502'; //                 x  |  box drawing vertical line
            case 121: return '\u2264'; //                 y  |  less than or equal to symbol
            case 122: return '\u2265'; //                 z  |  greater than or equal to symbol
            case 123: return '\u03C0'; // {    (left curly)  |  pi
            case 124: return '\u2260'; // | (vertical line)  |  not equal to symbol
            case 125: return '\u00A3'; // }   (right curly)  |  uk pound sign
            case 126: return '\u00B7'; // ~         (tilde)  |  centre dot
            default: return c;
        }
    }

    /**
     * Convert an integer to a Character array,
     * for example intToChars(1234) returns {'1', '2', '3', '4'}
     * @param number Integer to convert
     * @return Integer in Character array form
     */
    private synchronized Character[] intToChars(int number) {
        int n = number;
        Character[] c = new Character[(int) (Math.log10(n) + 1)];
        for (int i = c.length - 1; i >= 0; i--) {
            c[i] = (char)(48 + (n % 10));
            n /= 10;
        }
        return c;
    }

    /**
     * Converts a char array to a Character array.
     * @param chars char array.
     * @return Character array.
     */
    private synchronized Character[] chars(char chars[]) {
        Character c[] = new Character[chars.length];
        for (int i = 0; i < c.length; i++)
            c[i] = chars[i];
        return c;
    }

    /**
     * Crops the area inclusively between two rows of the terminal screen and returns it as a bitmap
     * @param top Top row of the terminal to crop. Must be <= rows.
     * @param bottom Bottom row of the terminal to crop. Must be <= rows and >= top.
     * @return Bitmap containing cropped area of screen.
     */
    private synchronized Bitmap bitCrop(int top, int bottom) {
        return Bitmap.createBitmap(bitmap, 0, paddingY+charHeight*(top), getWidth(),
                charHeight*(bottom-top+1)+1);
    }

    /**
     * Like bitCrop, but crops from the saved bitmaps (sbitmap and abitmap) rather than the current
     * bitmap (bitmap).
     * Also only crops one row rather than a range of rows.
     * This is used by the onScroll() and onFling() methods to retrieve rows from the screen buffer.
     * @param row Row of the terminal to crop. Must be <= rows.
     * @return Bitmap containing cropped area of screen.
     */
    private synchronized Bitmap bitCropS(int row) {
        if (isAlt) return Bitmap.createBitmap(abitmap, 0,
                paddingY+charHeight*(row), getWidth(), charHeight+1);
        else return Bitmap.createBitmap(sbitmap, 0,
                paddingY+charHeight*(row), getWidth(), charHeight+1);
    }

    /**
     * Paints a rectangle in the terminal bitmap using our internal row/column coordinate system.
     * @param left Column that is the left edge of the rectangle. Must be >= 0.
     * @param top Row that is the top edge of the rectangle. Must be >= 0.
     * @param right Column that is the right edge of the rectangle. Must be >= left and < cols.
     * @param bottom Row that is the bottom edge of the rectangle. Must be >= top and < rows.
     * @param paint Paint that defines the colour and style of the rectangle.
     *              This Paint cannot be null.
     */
    private synchronized void cBlockPaint(int left, int top, int right, int bottom, Paint paint) {
        canvas.drawRect(
                x(left),
                y(top) + charAscent,
                x(right + 1),
                y(bottom) + charDescent,
                paint);
    }

    /**
     * Shifts a rectangle in the terminal bitmap using our internal row/column coordinate system.
     * @param left Column that is the left edge of the rectangle. Must be >= 0.
     * @param top Row that is the top edge of the rectangle. Must be >= 0.
     * @param right Column that is the right edge of the rectangle. Must be >= left and < cols.
     * @param bottom Row that is the bottom edge of the rectangle. Must be >= top and < rows.
     * @param drows Amount of rows to shift the rectangle by (positive = down, negative = up).
     * @param dcols Amount of columns to shift the rectangle by (positive = right, negative = left).
     */
    private synchronized void cBlockShift(
            int left, int top, int right, int bottom, int drows, int dcols) {
        Bitmap bitBuffer = Bitmap.createBitmap(bitmap,
                x(left),
                y(top) + charAscent + 1,
                x(right + 1) - x(left),
                y(bottom) + charDescent + 1 - (y(top) + charAscent + 1));

        canvas.drawBitmap(bitBuffer,
                x(left) + charWidth*dcols,
                y(top) + charAscent + 1 + charHeight*drows,
                null);
    }

    /**
     * Shift a collection of characters left or right in the screen buffer of characters
     * (automatically wraps to next or previous row if necessary).
     * Be wary that the original copied characters are NOT erased,
     * but the characters at the destination are.
     * @param row Row in which the first (leftmost) character to copy is
     * @param col Column in which the first (leftmost) character to copy is
     * @param length Amount of characters to copy
     *               (reads additional characters to the right of the first character)
     * @param offset Horizontal distance to move (positive = right, negative = left)
     */
    private synchronized void bShift(int row, int col, int length, int offset) {
        char b[] = new char[length];
        int bF[] = new int[length];
        int bB[] = new int[length];
        byte bT[] = new byte[length];
        System.arraycopy(screenBuffer, pos(row, col), b, 0, length);
        System.arraycopy(screenBufferF, pos(row, col), bF, 0, length);
        System.arraycopy(screenBufferB, pos(row, col), bB, 0, length);
        System.arraycopy(screenBufferT, pos(row, col), bT, 0, length);
        System.arraycopy(b, 0, screenBuffer, pos(row, col) + offset, length);
        System.arraycopy(bF, 0, screenBufferF, pos(row, col) + offset, length);
        System.arraycopy(bB, 0, screenBufferB, pos(row, col) + offset, length);
        System.arraycopy(bT, 0, screenBufferT, pos(row, col) + offset, length);
    }

    /**
     * Clears a set of character spaces in the screen buffer using the DEFAULT BACKGROUND COLOUR.
     * @param row Row in which the first (leftmost) character to clear is
     * @param col Column in which the first (leftmost) character to clear is
     * @param length Amount of characters to clear
     *               (reads additional characters to the right of the first character)
     */
    private synchronized void bClear(int row, int col, int length) {
        char b[] = new char[length];
        int bF[] = new int[length];
        int bB[] = new int[length];
        byte bT[] = new byte[length];
        Arrays.fill(b, ' ');
        Arrays.fill(bF, fpaint.getColor());
        Arrays.fill(bB, dpaint.getColor());
        Arrays.fill(bT, (byte)0);
        System.arraycopy(b, 0, screenBuffer, pos(row, col), length);
        System.arraycopy(bF, 0, screenBufferF, pos(row, col), length);
        System.arraycopy(bB, 0, screenBufferB, pos(row, col), length);
        System.arraycopy(bT, 0, screenBufferT, pos(row, col), length);
    }

    /**
     * Clears a set of character spaces in the screen buffer using the CURRENT BACKGROUND COLOUR.
     * @param row Row in which the first (leftmost) character to clear is
     * @param col Column in which the first (leftmost) character to clear is
     * @param length Amount of characters to clear
     *               (reads additional characters to the right of the first character)
     */
    private synchronized void bClearE(int row, int col, int length) {
        char b[] = new char[length];
        int bF[] = new int[length];
        int bB[] = new int[length];
        byte bT[] = new byte[length];
        Arrays.fill(b, ' ');
        Arrays.fill(bF, fpaint.getColor());
        Arrays.fill(bB, bpaint.getColor());
        Arrays.fill(bT, (byte)0);
        System.arraycopy(b, 0, screenBuffer, pos(row, col), length);
        System.arraycopy(bF, 0, screenBufferF, pos(row, col), length);
        System.arraycopy(bB, 0, screenBufferB, pos(row, col), length);
        System.arraycopy(bT, 0, screenBufferT, pos(row, col), length);
    }

    /**
     * Convert Android scale-independent pixels to regular pixels.
     * This is used because text font size is in scale-independent pixels but
     * the fpaint object in the view uses regular pixels to set font size...
     * @param sp Value in scale-independent pixels to convert...
     * @return Value in regular pixels
     */
    private synchronized int spToPx(float sp) {
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    /**
     * Converts a List of Characters to an array of chars
     * @param L List of Characters
     * @return Array of chars
     */
    private synchronized char[] charLToA(List<Character> L) {
        char[] c = new char[L.size()];
        for (int i = 0; i < L.size(); i++) {
            c[i] = L.get(i);
        }
        return c;
    }

    /**
     * Converts a List of Integers to an array of ints
     * @param L List of Integers
     * @return Array of ints
     */
    private synchronized int[] intLToA(List<Integer> L) {
        int[] c = new int[L.size()];
        for (int i = 0; i < L.size(); i++) {
            c[i] = L.get(i);
        }
        return c;
    }

    /**
     * Gets the specified from screenBuffer
     * @param row Row number from screenBuffer which you want
     * @return The row you requested
     */
    private synchronized char[] getBufferRow(int row) {
        char b[] = new char[cols];
        for (int i = 0; i < cols; i++) {
            b[i] = screenBuffer[pos(row, i)];
        }
        return b;
    }

    /**
     * Outputs the index of the specified character in screenBuffer
     * @param row Row in which the character resides
     * @param col Column in which the character resides
     * @return Index to the character, you can get the character itself using
     * screenBuffer[pos(row, col)];
     */
    private synchronized int pos(int row,int col){return isAlt?row*cols+col+rows*cols:row*cols+col;}

    /**
     * Detects whether or not specified char is a number from 0-9
     * @param c Char to test
     * @return Whether or not the char was a number
     */
    private synchronized boolean isNum(char c) { return (int)c>47&&(int)c<58; }

    /**
     * If the input char is a number, it will be converted to an int
     * @param c Char to convert
     * @return Char in numeric form (if the char was a number, otherwise -1)
     */
    private synchronized int toNum(char c) {
        if (isNum(c)) {
            return (byte)c-48;
        } else {
            return -1;
        }
    }

    /**
     * If the input chars are all numbers,
     * they will be converted to a number where each char is one digit.
     * @param c Chars to convert
     * @return Chars in numeric form (if all chars were numbers, otherwise -1)
     */
    private synchronized int toNum(char c[]) {
        if (c.length == 0) return -1;
        int i = (int)Math.pow(10, (c.length-1));
        int r = 0;
        for (char x : c) {
            if (!isNum(x)) return -1;
            r += toNum(x)*i;
            i /= 10;
        }
        return r;
    }

    /**
     * Finds the x-ordinate of the LEFT edge of nth column (in regular pixels)
     * @param n Zero-based index of the column
     * @return X-ordinate of the leftmost edge of nth column
     */
    private synchronized int x(int n) { return charWidth * n + paddingX; }

    /**
     * Finds the y-ordinate of the TEXT BASELINE of nth row (in regular pixels)
     * @param n Zero-based index of the row
     * @return Y-ordinate of the text baseline of nth row
     */
    private synchronized int y(int n) { return charDescent*n - charAscent*(n+1) + paddingY; }

    /**
     * This is run at times to automatically compute size of terminal in rows and columns
     * @param oldRows The number of rows there were before the resize
     */
    private synchronized void getSize(int oldRows) {
        Rect w = new Rect();
        fpaint.getTextBounds("A", 0,1, w);

        Rect h = new Rect();
        fpaint.getTextBounds("ly", 0,2, h);

        // Modern terminal emulators make the character bounding box approximately
        // twice as tall as it is wide
        charWidth = w.width();
        charHeight = w.width()*2;

        charAscent = h.top - charHeight + h.height();
        charDescent = h.bottom;

        rows = (this.getHeight() - paddingY*2) / charHeight;
        cols = (this.getWidth() - paddingX*2) / charWidth;

        if (scrollEnd == oldRows - 1) scrollEnd = rows - 1;
        if (ascrollEnd == oldRows - 1) ascrollEnd = rows - 1;
    }


























    // Inherited methods (from View and from GestureDetector.OnGestureListener)
    // and their subroutines

    @Override public synchronized void onShowPress(MotionEvent e) { gesture.onShowPress(e); }

    @Override public synchronized void onLongPress(MotionEvent e) { gesture.onLongPress(e); }

    /**
     * This is run whenever the user touches the screen, whether it be to tap, long tap or scroll.
     * @param e The ACTION_DOWN motion event.
     */
    @Override public synchronized boolean onDown(MotionEvent e) {
        mouseDown = true;
        mouseDiff = false;
        doMouseTracking(getMouseTrackingMode(), e.getX(), e.getY(), mouseDown, ctrlDown, shiftDown,
                metaDown, mouseDiff);
        gesture.onDown(e);
        return true;
    }

    /**
     * When the user taps the screen, this is run when the user releases their finger.
     * @param e The ACTION_UP motion event.
     */
    @Override public synchronized boolean onSingleTapUp(MotionEvent e) {
        gesture.onSingleTapUp(e);
        return true;
    }

    /**
     * This is called when the user scrolls the screen by dragging their finger across the screen.
     */
    @Override public synchronized boolean onScroll(MotionEvent e1,MotionEvent e2,float dx,float dy){
        // Only do scrolling if Mouse Tracking is off
        if (getMouseTrackingMode() == 0) {
            scrollY -= dy;// Update scrolling value in pixels
            if (scrollY < 0) scrollY = 0; // Prevent scrolling to below the screen buffer
            if ((int) scrollY / charHeight > scrollback.size())
                // Prevent scrolling to above the scrollback
                scrollY = scrollback.size() * charHeight;

            int toScroll = (int) scrollY / charHeight - scrollLines;
            if (toScroll > rows - 2) toScroll = rows - 2;
            if (toScroll < -rows + 2) toScroll = -rows + 2;

            // If the scrolling value in pixels does not match the scrolling value in lines,
            // then we need to scroll
            if (toScroll > 0) //Scrolling down (user drags finger downwards, scrollbar goes upwards)
                scrollScreenDown(toScroll); // Shift contents of screen downwards
            if (toScroll < 0) // Scrolling up (user drags finger upwards, scrollbar goes downwards)
                scrollScreenUp(-toScroll); // Shift contents of screen upwards

            // Update scrolling value in lines
            scrollLines = (int) scrollY / charHeight;
        }

        // Do Mouse Tracking for scrolling if the mode is Button-Event or Any-Event
        if (getMouseTrackingMode() > 2) {
            mouseDown = true;
            mouseDiff = true;
            doMouseTracking(getMouseTrackingMode(), e2.getX(), e2.getY(), mouseDown,
                    ctrlDown, shiftDown, metaDown, mouseDiff);
        }

        gesture.onScroll(e1, e2, dx, dy);
        return true;
    }

    /**
     * This is called when the user scrolls the screen by flicking with their finger.
     */
    @Override public synchronized boolean onFling(MotionEvent e1,MotionEvent e2,float dx,float dy) {
        gesture.onFling(e1, e2, dx, dy);
        return true;
    }

    /**
     * This is called whenever the user releases their finger.
     * @param e Up MotionEvent where the user released their finger.
     */
    private synchronized void onUp(MotionEvent e) {
        // Do not report mouse up events if Mouse Tracking mode is X10 Compatibility Mode
        if (getMouseTrackingMode() > 1) {
            mouseDown = false;
            mouseDiff = false;
            doMouseTracking(getMouseTrackingMode(), e.getX(), e.getY(), mouseDown, ctrlDown,
                    shiftDown, metaDown, mouseDiff);
        }
    }

    /**
     * Called when any touch event occurs.
     * @param e MotionEvent.
     */
    private synchronized void onMotionEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP)
            onUp(e);

        gestureDetector.onTouchEvent(e);
    }

    /**
     * This is called on initialization.
     */
    private synchronized void init() {
        // Setup gesture detection
        gestureDetector = new GestureDetector(context, this, null);
        scroller = new OverScroller(context);
        setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                onTouchListener.onTouch(v, e);
                onMotionEvent(e);
                return true;
            }
        });

        // Setup typefaces
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "AnkaCoder_regular.ttf");
        Typeface fontBold = Typeface.createFromAsset(context.getAssets(),
                "AnkaCoder_bold.ttf");

        // Initialize the Paint object for drawing regular text
        fpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fpaint.setColor(fcolor);
        fpaint.setStyle(Paint.Style.FILL);
        fpaint.setTextSize(spToPx(Global.getInstance().fontSize));
        fpaint.setTypeface(font);

        // Initialize the Paint object for drawing bold text
        fpaintbold = new Paint(Paint.ANTI_ALIAS_FLAG);
        fpaintbold.setColor(fcolor);
        fpaintbold.setStyle(Paint.Style.FILL);
        fpaintbold.setTextSize(spToPx(Global.getInstance().fontSize));
        fpaintbold.setTypeface(fontBold);
        fpaintbold.setFakeBoldText(true);

        // Initialize the Paint object for drawing the text background
        bpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bpaint.setColor(bcolor);
        bpaint.setStyle(Paint.Style.FILL);

        // Initialize the Paint object for drawing the cursor
        cpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cpaint.setColor(ccolor);
        cpaint.setStyle(Paint.Style.FILL);

        // Initialize the Paint object for drawing regular characters inverted on top of the cursor
        ipaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ipaint.setColor(bcolor);
        ipaint.setStyle(Paint.Style.FILL);
        ipaint.setTextSize(spToPx(Global.getInstance().fontSize));
        ipaint.setTypeface(font);

        // Initialize the Paint object for drawing bold characters inverted on top of the cursor
        ipaintbold = new Paint(Paint.ANTI_ALIAS_FLAG);
        ipaintbold.setColor(bcolor);
        ipaintbold.setStyle(Paint.Style.FILL);
        ipaintbold.setTextSize(spToPx(Global.getInstance().fontSize));
        ipaintbold.setTypeface(fontBold);
        ipaintbold.setFakeBoldText(true);

        // Initialize the Paint object for drawing the default coloured text background
        dpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dpaint.setColor(dcolor);
        dpaint.setStyle(Paint.Style.FILL);

        // Initialize the Paint object for drawing transparent pixels
        tpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tpaint.setColor(0);
        tpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        tpaint.setStyle(Paint.Style.FILL);
    }

    /**
     * This is also called on initialization, but AFTER init(),
     * and only if this class is instantiated with attributes.
     * It sets the attributes of this class depending on the XML tags.
     */
    private synchronized void initAttr(AttributeSet attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attr, R.styleable.xtermView, 0, 0);
        try {
            paddingX = a.getDimensionPixelSize(R.styleable.xtermView_horizontalPadding, 0);
            paddingY = a.getDimensionPixelSize(R.styleable.xtermView_verticalPadding, 0);
        } finally {
            a.recycle();
        }
    }

    /**
     * This is called when the size of the terminal changes, and, on initialization,
     * is called AFTER init().
     */
    @Override protected synchronized void onSizeChanged(int w, int h, int oldw, int oldh) {
        requestLayout();

        int _rows = rows;
        int _cols = cols;
        getSize(_rows);

        // This runs on startup after layout has been acquired
        if (bitmap == null) {
            scrollEnd = rows - 1;
            ascrollEnd = rows - 1;

            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(colors[1]);

            sbitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            scanvas = new Canvas(sbitmap);
            abitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            acanvas = new Canvas(abitmap);

            tbitmap = Bitmap.createBitmap(charWidth, charHeight, Bitmap.Config.ARGB_8888);
            tcanvas = new Canvas(tbitmap);

            screenBuffer = new char[rows * cols * 2]; // Initialize screenBuffer
            Arrays.fill(screenBuffer, ' '); // Fill screenBuffer with spaces

            screenBufferF = new int[rows * cols * 2]; // Initialize screenBufferF
            Arrays.fill(screenBufferF, colors[0]); // Fill screenBuffer with default foreground

            screenBufferB = new int[rows * cols * 2]; // Initialize screenBufferB
            Arrays.fill(screenBufferB, colors[1]); // Fill screenBuffer with default background

            screenBufferT = new byte[rows * cols * 2]; // Initialize screenBufferT
            Arrays.fill(screenBufferT, (byte)0); // Fill screenBuffer with 0 (regular text)

            // Print any characters to the screen that were sent before the
            // terminal was fully initialised
            ready = true;
            write(charLToA(preBuffer), preBuffer.size());
        }

        // This runs if the view is resized by the user
        // (changing orientation, showing soft keyboard, etc...)
        else {
            // If the user has scrolled the window, scroll to the bottom
            if (scrollLines > 0) {
                scrollY = 0;
                scrollLines = 0;
                if (isAlt) canvas.drawBitmap(abitmap, 0, 0, null);
                else canvas.drawBitmap(sbitmap, 0, 0, null);
            }

            // If view was resized and has less rows than before
            if (rows < _rows) {
                int L = rows<curY ? curY-rows+1 : 0;
                if (L>0) { // This runs if the cursor is below the screen after resizing
                    // Add top L lines to scrollback
                    for (int i = 0; i < L; i++)
                        scrollback.add(0, bitCrop(i, i));
                    // Scroll window up L lines
                    scrollScreenUp(L);
                    // Shift all but the first L rows upwards by one row
                    bShift(L, 0, (_rows - L) * cols, (-cols) * L);
                    // Clear the last L rows because they're now duplicates
                    bClear(_rows - L, 0, cols * L);
                    // Move the cursor to the bottom row
                    curY -= L;
                    // Draw over the bottom few rows of the screen that were removed
                    canvas.drawRect(0, paddingY+rows*charHeight, w, oldh, bpaint);
                }

                // Resize screenbuffers (copy data from first rows of screenbuffers)
                char b[] = new char[rows * cols * 2];
                int bF[] = new int[rows * cols * 2];
                int bB[] = new int[rows * cols * 2];
                byte bT[] = new byte[rows * cols * 2];
                Arrays.fill(b, ' ');
                Arrays.fill(bF, colors[0]);
                Arrays.fill(bB, colors[1]);
                Arrays.fill(bT, (byte)0);
                System.arraycopy(screenBuffer, 0, b, 0, rows*cols);
                System.arraycopy(
                        screenBuffer, _rows*cols, b, rows*cols, rows*cols);
                System.arraycopy(screenBufferF, 0, bF, 0, rows*cols);
                System.arraycopy(
                        screenBufferF, _rows*cols, bF, rows*cols, rows*cols);
                System.arraycopy(screenBufferB, 0, bB, 0, rows*cols);
                System.arraycopy(
                        screenBufferB, _rows*cols, bB, rows*cols, rows*cols);
                System.arraycopy(screenBufferT, 0, bT, 0, rows*cols);
                System.arraycopy(
                        screenBufferT, _rows*cols, bT, rows*cols, rows*cols);
                screenBuffer = b;
                screenBufferF = bF;
                screenBufferB = bB;
                screenBufferT = bT;
            }

            // If view was resized and has more rows than before
            if (rows > _rows) {
                //int L = scrollback.size()>=rows-curY ? rows-curY : 0;
                //scrollScreenDown(rows - curY);

                // Resize screenbuffers (copy data from first rows of screenbuffers)
                char b[] = new char[rows * cols * 2];
                int bF[] = new int[rows * cols * 2];
                int bB[] = new int[rows * cols * 2];
                byte bT[] = new byte[rows * cols * 2];
                Arrays.fill(b, ' ');
                Arrays.fill(bF, colors[0]);
                Arrays.fill(bB, colors[1]);
                Arrays.fill(bT, (byte)0);
                System.arraycopy(screenBuffer, 0, b, 0, _rows*cols);
                System.arraycopy(
                        screenBuffer, _rows*cols, b, rows*cols, _rows*cols);
                System.arraycopy(screenBufferF, 0, bF, 0, _rows*cols);
                System.arraycopy(
                        screenBufferF, _rows*cols,bF, rows*cols, _rows*cols);
                System.arraycopy(screenBufferB, 0, bB, 0, _rows*cols);
                System.arraycopy(
                        screenBufferB, _rows*cols,bB, rows*cols, _rows*cols);
                System.arraycopy(screenBufferT, 0, bT, 0, _rows*cols);
                System.arraycopy(
                        screenBufferT, _rows*cols,bT, rows*cols, _rows*cols);
                screenBuffer = b;
                screenBufferF = bF;
                screenBufferB = bB;
                screenBufferT = bT;
            }
        }
    }

    /**
     * This is called when we use invalidate() and, on initialization,
     * is called AFTER onSizeChanged().
     */
    @Override protected synchronized void onDraw(Canvas c) {
        c.drawBitmap(bitmap, 0, 0, fpaint); // Re-render the view

        // Draw the cursor
        if (scrollLines < rows - curY && cursorShown)
            c.drawRect(x(curX),
                    y(curY) + charAscent + scrollLines*charHeight,
                    x(curX + 1),
                    y(curY) + charDescent + scrollLines*charHeight,
                    cpaint); // Re-render the cursor

        // Draw the inverted character on the cursor
        if (scrollLines < rows - curY && cursorShown)
            c.drawText(String.valueOf(screenBuffer[pos(curY, curX)]), x(curX),
                    y(curY + scrollLines), screenBufferT[pos(curY, curX)]>0 ? ipaintbold:ipaint);

        // Save the current screen buffer for quick switching later
        if (scrollLines == 0) {
            if (isAlt) acanvas.drawBitmap(bitmap, 0, 0, null);
            else scanvas.drawBitmap(bitmap, 0, 0, null);
        }
    }























    // Constructors
    public xtermView(Context c) {
        super(c);
        context = c;
        init();
    }
    public xtermView(Context c, AttributeSet attr) {
        super(c, attr);
        context = c;
        init();
        initAttr(attr);
    }
    public xtermView(Context c, AttributeSet attr, int defStyleAttr) {
        super(c, attr, defStyleAttr);
        context = c;
        init();
        initAttr(attr);
    }






















    // Fields

    /**
     * Colours for the terminal are stored here.
     * By default, colours 0 to 15 are based off of ubuntu's default theme for gnome-terminal.
     * The colours 16 to 255 are the colours from xterm-256color, which you can find here:
     * <pre>https://jonasjacek.github.io/colors/</pre>
     * Alternatively use this one, but I spotted an error on this one, the colours 178 to 195
     * start with "df" on the website but they should start with "d7".
     * <pre>https://upload.wikimedia.org/wikipedia/commons/1/15/Xterm_256color_chart.svg</pre>
     */
    public int colors[] = {
            0xffd3d7cf, // -3  default foreground (white)
            0xff000000, // -2  default background (black)
            0xff00ff00, // -1  default cursor (green)

            0xff000000, // 0   black
            0xffcc0000, // 1   red
            0xff4e9a06, // 2   green
            0xffc4a000, // 3   yellow
            0xff3465a4, // 4   blue
            0xff75507b, // 5   magenta (looks more like purple)
            0xff06989a, // 6   cyan
            0xffd3d7cf, // 7   white (not pure white)

            0xff555753, // 8   light black (gray)
            0xffef2929, // 9   light red
            0xff8ae234, // 10  light green
            0xfffce94f, // 11  light yellow
            0xff729fcf, // 12  light blue
            0xffad7fa8, // 13  light magenta (looks more like light purple)
            0xff34e2e2, // 14  light cyan
            0xffffffff, // 15  light white (pure white)

            // The rest of these colours are the xterm-256colors colours
            0xff000000, // 16
            0xff00005f, // 17
            0xff000087, // 18
            0xff0000af, // 19
            0xff0000d7, // 20
            0xff0000ff, // 21

            0xff005f00, // 22
            0xff005f5f, // 23
            0xff005f87, // 24
            0xff005faf, // 25
            0xff005fd7, // 26
            0xff005fff, // 27

            0xff008700, // 28
            0xff00875f, // 29
            0xff008787, // 30
            0xff0087af, // 31
            0xff0087d7, // 32
            0xff0087ff, // 33

            0xff00af00, // 34
            0xff00af5f, // 35
            0xff00af87, // 36
            0xff00afaf, // 37
            0xff00afd7, // 38
            0xff00afff, // 39

            0xff00d700, // 40
            0xff00d75f, // 41
            0xff00d787, // 42
            0xff00d7af, // 43
            0xff00d7d7, // 44
            0xff00d7ff, // 45

            0xff00ff00, // 46
            0xff00ff5f, // 47
            0xff00ff87, // 48
            0xff00ffaf, // 49
            0xff00ffd7, // 50
            0xff00ffff, // 51

            0xff5f0000, // 52
            0xff5f005f, // 53
            0xff5f0087, // 54
            0xff5f00af, // 55
            0xff5f00d7, // 56
            0xff5f00ff, // 57

            0xff5f5f00, // 58
            0xff5f5f5f, // 59
            0xff5f5f87, // 60
            0xff5f5faf, // 61
            0xff5f5fd7, // 62
            0xff5f5fff, // 63

            0xff5f8700, // 64
            0xff5f875f, // 65
            0xff5f8787, // 66
            0xff5f87af, // 67
            0xff5f87d7, // 68
            0xff5f87ff, // 69

            0xff5faf00, // 70
            0xff5faf5f, // 71
            0xff5faf87, // 72
            0xff5fafaf, // 73
            0xff5fafd7, // 74
            0xff5fafff, // 75

            0xff5fd700, // 76
            0xff5fd75f, // 77
            0xff5fd787, // 78
            0xff5fd7af, // 79
            0xff5fd7d7, // 80
            0xff5fd7ff, // 81

            0xff5fff00, // 82
            0xff5fff5f, // 83
            0xff5fff87, // 84
            0xff5fffaf, // 85
            0xff5fffd7, // 86
            0xff5fffff, // 87

            0xff870000, // 88
            0xff87005f, // 89
            0xff870087, // 90
            0xff8700af, // 91
            0xff8700d7, // 92
            0xff8700ff, // 93

            0xff875f00, // 94
            0xff875f5f, // 95
            0xff875f87, // 96
            0xff875faf, // 97
            0xff875fd7, // 98
            0xff875fff, // 99

            0xff878700, // 100
            0xff87875f, // 101
            0xff878787, // 102
            0xff8787af, // 103
            0xff8787d7, // 104
            0xff8787ff, // 105

            0xff87af00, // 106
            0xff87af5f, // 107
            0xff87af87, // 108
            0xff87afaf, // 109
            0xff87afd7, // 110
            0xff87afff, // 111

            0xff87d700, // 112
            0xff87d75f, // 113
            0xff87d787, // 114
            0xff87d7af, // 115
            0xff87d7d7, // 116
            0xff87d7ff, // 117

            0xff87ff00, // 118
            0xff87ff5f, // 119
            0xff87ff87, // 120
            0xff87ffaf, // 121
            0xff87ffd7, // 122
            0xff87ffff, // 123

            0xffaf0000, // 124
            0xffaf005f, // 125
            0xffaf0087, // 126
            0xffaf00af, // 127
            0xffaf00d7, // 128
            0xffaf00ff, // 129

            0xffaf5f00, // 130
            0xffaf5f5f, // 131
            0xffaf5f87, // 132
            0xffaf5faf, // 133
            0xffaf5fd7, // 134
            0xffaf5fff, // 135

            0xffaf8700, // 136
            0xffaf875f, // 137
            0xffaf8787, // 138
            0xffaf87af, // 139
            0xffaf87d7, // 140
            0xffaf87ff, // 141

            0xffafaf00, // 142
            0xffafaf5f, // 143
            0xffafaf87, // 144
            0xffafafaf, // 145
            0xffafafd7, // 146
            0xffafafff, // 147

            0xffafd700, // 148
            0xffafd75f, // 149
            0xffafd787, // 150
            0xffafd7af, // 151
            0xffafd7d7, // 152
            0xffafd7ff, // 153

            0xffafff00, // 154
            0xffafff5f, // 155
            0xffafff87, // 156
            0xffafffaf, // 157
            0xffafffd7, // 158
            0xffafffff, // 159

            0xffd70000, // 160
            0xffd7005f, // 161
            0xffd70087, // 162
            0xffd700af, // 163
            0xffd700d7, // 164
            0xffd700ff, // 165

            0xffd75f00, // 166
            0xffd75f5f, // 167
            0xffd75f87, // 168
            0xffd75faf, // 169
            0xffd75fd7, // 170
            0xffd75fff, // 171

            0xffd78700, // 172
            0xffd7875f, // 173
            0xffd78787, // 174
            0xffd787af, // 175
            0xffd787d7, // 176
            0xffd787ff, // 177

            0xffd7af00, // 178
            0xffd7af5f, // 179
            0xffd7af87, // 180
            0xffd7afaf, // 181
            0xffd7afd7, // 182
            0xffd7afff, // 183

            0xffd7d700, // 184
            0xffd7d75f, // 185
            0xffd7d787, // 186
            0xffd7d7af, // 187
            0xffd7d7d7, // 188
            0xffd7d7ff, // 189

            0xffd7ff00, // 190
            0xffd7ff5f, // 191
            0xffd7ff87, // 192
            0xffd7ffaf, // 193
            0xffd7ffd7, // 194
            0xffd7ffff, // 195

            0xffff0000, // 196
            0xffff005f, // 197
            0xffff0087, // 198
            0xffff00af, // 199
            0xffff00d7, // 200
            0xffff00ff, // 201

            0xffff5f00, // 202
            0xffff5f5f, // 203
            0xffff5f87, // 204
            0xffff5faf, // 205
            0xffff5fd7, // 206
            0xffff5fff, // 207

            0xffff8700, // 208
            0xffff875f, // 209
            0xffff8787, // 210
            0xffff87af, // 211
            0xffff87d7, // 212
            0xffff87ff, // 213

            0xffffaf00, // 214
            0xffffaf5f, // 215
            0xffffaf87, // 216
            0xffffafaf, // 217
            0xffffafd7, // 218
            0xffffafff, // 219

            0xffffd700, // 220
            0xffffd75f, // 221
            0xffffd787, // 222
            0xffffd7af, // 223
            0xffffd7d7, // 224
            0xffffd7ff, // 225

            0xffffff00, // 226
            0xffffff5f, // 227
            0xffffff87, // 228
            0xffffffaf, // 229
            0xffffffd7, // 230
            0xffffffff, // 231

            0xff080808, // 232
            0xff121212, // 233
            0xff1c1c1c, // 234
            0xff262626, // 235
            0xff303030, // 236
            0xff3a3a3a, // 237
            0xff444444, // 238
            0xff4e4e4e, // 239
            0xff585858, // 240
            0xff626262, // 241
            0xff6c6c6c, // 242
            0xff767676, // 243
            0xff808080, // 244
            0xff8a8a8a, // 245
            0xff949494, // 246
            0xff9e9e9e, // 247
            0xffa8a8a8, // 248
            0xffb2b2b2, // 249
            0xffbcbcbc, // 250
            0xffc6c6c6, // 251
            0xffd0d0d0, // 252
            0xffdadada, // 253
            0xffe4e4e4, // 254
            0xffeeeeee, // 255
    };

    /**
     * This defines colours and styles for the text foreground.
     */
    private Paint fpaint;

    /**
     * This defines colours and styles for the text foreground when the text is bold.
     */
    private Paint fpaintbold;

    /**
     * This defines colours and styles for the text background.
     */
    private Paint bpaint;

    /**
     * This defines colours and styles for the cursor.
     */
    private Paint cpaint;

    /**
     * This define colours and styles for the character that gets drawn inverted on the cursor.
     */
    private Paint ipaint;

    /**
     * This define colours and styles for the default background.
     */
    private Paint dpaint;

    /**
     * This define colours and styles for the character that
     * gets drawn inverted on the cursor when the text is bold/
     */
    private Paint ipaintbold;

    /**
     * This paint is always completely transparent.
     */
    private Paint tpaint;

    /**
     * This bitmap stores the data in the terminal as an image.
     */
    private Bitmap bitmap = null;

    /**
     * This canvas allows us to modify the bitmap for the terminal.
     */
    private Canvas canvas = null;

    /**
     * This is the saved bitmap for the normal screen buffer.
     */
    private Bitmap sbitmap = null;

    /**
     * This canvas corresponds to the saved bitmap for the normal screen buffer.
     */
    private Canvas scanvas = null;

    /**
     * This is the saved bitmap for the alternate screen buffer.
     */
    private Bitmap abitmap = null;

    /**
     * This canvas corresponds to the saved bitmap for the alternate screen buffer.
     */
    private Canvas acanvas = null;

    /**
     * This is the bitmap used to temporarily hold characters for processing.
     */
    private Bitmap tbitmap = null;

    /**
     * This canvas corresponds to the bitmap used to temporarily hold characters for processing.
     */
    private Canvas tcanvas = null;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Maximum number of rows that can fit on the screen.
     */
    private int rows;

    /**
     * Maximum number of columns that can fit on the screen.
     */
    private int cols;

    /**
     * First row in the screen buffer that is part of the scrolling region.
     */
    private int scrollStart = 0;

    /**
     * Last row in the screen buffer that is part of the scrolling region.
     */
    private int scrollEnd;

    /**
     * First row in the screen buffer other than the one we're using that is
     * part of the scrolling region.
     */
    private int ascrollStart = 0;

    /**
     * Last row in the screen buffer other than the one we're using that is
     * part of the scrolling region.
     */
    private int ascrollEnd;

    /**
     * Maximum width of one character in pixels.
     */
    private int charWidth;

    /**
     * Maximum height of one character in pixels, with line spacing removed.
     */
    private int charHeight;

    /**
     * True distance from baseline to ascent of a character in pixels, with line spacing removed,
     * this value is always negative.
     */
    private int charAscent;

    /**
     * Distance from baseline to descent of a character in pixels, this value is always positive.
     */
    private int charDescent;

    /**
     * This list contains all the characters ever printed by the terminal,
     * including escape codes and non-readable characters.
     */
    private List<Character> transcript1 = new ArrayList<>();

    /**
     * This list contains all the characters ever printed by the terminal,
     * including escape codes but excluding non-readable characters.
     */
    private List<Character> transcript2 = new ArrayList<>();

    /**
     * This list contains all the characters ever printed by the terminal,
     * excluding escape codes but including non-readable characters.
     */
    private List<Character> transcript3 = new ArrayList<>();

    /**
     * This list contains all the characters ever printed by the terminal,
     * excluding escape codes and non-readable characters.
     */
    private List<Character> transcript4 = new ArrayList<>();

    /**
     * Column the cursor is in.
     */
    private int curX = 0;

    /**
     * Row the cursor is in.
     */
    private int curY = 0;

    /**
     * Current foreground colour.
     */
    private int fcolor = colors[0];

    /**
     * Current background colour.
     */
    private int bcolor = colors[1];

    /**
     * Current cursor colour.
     */
    private int ccolor = colors[2];

    /**
     * Default background colour.
     */
    private int dcolor = colors[1];

    /**
     * Horizontal padding.
     */
    private int paddingX = 0;

    /**
     * Vertical padding.
     */
    private int paddingY = 0;

    /**
     * Whether or not render() is currently looking for escape codes.
     * This is set to true when an escape character is detected.
     * It is set to false when the entire escape code has been received.
     */
    private boolean isEscaping = false;

    /**
     * If isEscaping is set to true,
     * this holds characters in the escape code until the entire code is received.
     * The first character in the escape code (the escape character) is omitted.
     */
    private List<Character> e = new ArrayList<>();

    /**
     * Whether or not reverse video is enabled.
     * If reverse video is enabled, the foreground and background colours are swapped.
     */
    private boolean isReversed = false;

    /**
     * Runnable to run when bell character is received.
     */
    private Runnable onBell = new Runnable() { public void run() { } };

    /**
     * Allows you to add your own touch events to the view.
     */
    private GestureDetector.OnGestureListener gesture=new GestureDetector.SimpleOnGestureListener();

    /**
     * This object is used to send information to the main application to write data to whatever it
     * is communicating to.
     */
    private callback shell = new callback() {
        public void output(char[] c) {
            Log.w("xtermView",
                    "WARNING: xtermView tried to send data," +
                            "but callback was not set using setCallback() !!!!");
        }
    };

    /**
     * This allows you to add code to this View's internal OnTouchListener.
     */
    private OnTouchListener onTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            v.performClick();
            return false;
        }
    };

    /**
     * Runnable to run when keypad changes from "Normal Keypad" to "Application Keypad",
     * or vice versa.
     */
    private Runnable onKeypadChange = new Runnable() { public void run() { } };

    /**
     * Runnable to run when cursor keys change from
     * "Normal Cursor Keys" to "Application Cursor Keys", or vice versa.
     */
    private Runnable onCursorKeysChange = new Runnable() { public void run() { } };

    /**
     * Runnable to run when screen buffer changes from
     * "Normal Screen Buffer" to "Alternate Screen Buffer", or vice versa.
     */
    private Runnable onScreenBufferChange = new Runnable() { public void run() { } };

    /**
     * Runnable to run when cursor is shown or hidden.
     */
    private Runnable onCursorShownHidden = new Runnable() { public void run() { } };

    /**
     * Runnable to run when the DEC special graphics set is enabled or disabled.
     */
    private Runnable onDecSpecGraph = new Runnable() { public void run() { } };

    /**
     * Runnable to run when Mouse Tracking mode changes.
     */
    private Runnable onMouseTrackingChange = new Runnable() { public void run() { } };

    /**
     * Runnable to run when Highlight Tracking is enabled or disabled.
     */
    private Runnable onHighlightTrackingChange = new Runnable() { public void run() { } };

    /**
     * Runnable to run when xterm window title changes.
     */
    private Runnable onTitleChange = new Runnable() { public void run() { } };

    /**
     * Contains only the on-screen characters and the Alternate Screen Buffer.
     */
    private char screenBuffer[];

    /**
     * Contains the foreground colours for the on-screen characters and the Alternate Screen Buffer.
     */
    private int screenBufferF[];

    /**
     * Contains the background colours for the on-screen characters and the Alternate Screen Buffer.
     */
    private int screenBufferB[];

    /**
     * Contains text typeface for on-screen characters and the Alternate Screen Buffer.
     * 0 is regular, 1 is bold.
     */
    private byte screenBufferT[];

    /**
     * This List contains each line in the scrollback as a bitmap.
     * The newest lines added to scrollback are at the start, the oldest lines are at the end.
     */
    private List<Bitmap> scrollback = new ArrayList<>();

    /**
     * Current typeface.
     * 0 is regular, 1 is bold.
     */
    private byte typeface = 0;

    /**
     * Whether or not we are using the Alternate Screen Buffer.
     */
    private boolean isAlt = false;

    /**
     * Saved curX position (from DECSC).
     */
    private int scurX = 0;

    /**
     * Saved curY position (from DECSC).
     */
    private int scurY = 0;

    /**
     * Saved curX position (from DECSET).
     */
    private int acurX = 0;

    /**
     * Saved curY position (from DECSET).
     */
    private int acurY = 0;

    /**
     * Saved curX position (from ANSI.SYS).
     */
    private int ncurX = 0;

    /**
     * Saved curY position (from ANSI.SYS).
     */
    private int ncurY = 0;

    /**
     * Object used for gathering data about scrolling.
     */
    private OverScroller scroller = null;

    /**
     * Object used to detect motion events such as scrolling.
     */
    private GestureDetector gestureDetector = null;

    /**
     * Distance scrolled into the scroll buffer (in pixels).
     */
    private float scrollY = 0;

    /**
     * Distance scrolled into the scroll buffer (in lines).
     */
    private int scrollLines = 0;

    /**
     * Whether or not the terminal should auto-scroll when we go past the bottom row.
     * This is disabled when the cursor position is set manually by an escape code,
     * and re-enabled when we receive a newline or the cursor reaches the end of a row.
     */
    private boolean shouldScroll = true;

    /**
     * If this is false, line feed characters (character code 10) cause the cursor to move to the
     * next line without changing the cursor column. If this is true, line feed characters cause
     * the cursor to move to the beginning of the next line.
     */
    private boolean autoNewline = false;

    /**
     * Keypad type (true = Application Keypad, false = Normal Keypad).
     * The escape codes to send when keys are pressed are different for
     * "Application Mode" and "Normal Mode".
     * The keypad type affects the
     * numbers 0-9 on the keypad as well as + - * / = . , from the keypad and enter from the keypad.
     * Check here for more details:
     * <pre>https://www.gnu.org/software/screen/manual/screen.html#Input-Translation</pre>
     * If you are connected to a UNIX shell check your keypad type using
     * xtermView.isApplicationKeypad() frequently.
     */
    private boolean appKeypad = false;

    /**
     * Cursor keys type (true = Application Cursor Keys, false = Normal Cursor Keys).
     * The escape codes to send when keys are pressed are different for
     * "Application Mode" and "Normal Mode".
     * The cursor keys type affects how the cursor keys (arrow keys) on the keyboard are processed.
     * Check here for more details:
     * <pre>https://www.gnu.org/software/screen/manual/screen.html#Input-Translation</pre>
     * If you are connected to a UNIX shell check your cursor keys type using
     * xtermView.isApplicationCursorKeys() frequently.
     */
    private boolean appCursorKeys = false;

    /**
     * Whether or not cursor is shown.
     */
    private boolean cursorShown = true;

    /**
     * Whether or not xtermView should use DEC's special graphics encoding.
     * If this is enabled, the characters 95 to 126 (decimal, inclusive)
     * will be replaced with the character with the same charcode
     * from the DEC special graphics set. The DEC special graphics set can be seen here:
     * <pre>https://en.wikipedia.org/wiki/DEC_Special_Graphics</pre>
     * Also look at this website:
     * <pre>https://vt100.net/docs/vt100-ug/table3-9.html</pre>
     */
    private boolean decSpecGraph = false;

    /**
     * Whether or not X10 Compatibility Mouse Tracking is enabled.
     */
    private boolean mouseTracking_x10compat = false;

    /**
     * Whether or not Normal Mouse Tracking is enabled.
     */
    private boolean mouseTracking_normal = false;

    /**
     * Whether or not Highlight Tracking is enabled.
     */
    private boolean mouseTracking_highlight = false;

    /**
     * Whether or not Button-Event Mouse Tracking is enabled.
     */
    private boolean mouseTracking_button = false;

    /**
     * Whether or not Any-Event Mouse Tracking is enabled.
     */
    private boolean mouseTracking_any = false;

    /**
     * X co-ordinate of the mouse relative to the screen.
     */
    private float mouseX = 0;

    /**
     * Y co-ordinate of the mouse relative to the screen.
     */
    private float mouseY = 0;

    /**
     * Column the mouse is in.
     */
    private int mouseCX = 0;

    /**
     * Row the mouse is in.
     */
    private int mouseCY = 0;

    /**
     * Whether or not the left mouse button is held down.
     */
    private boolean mouseDown = false;

    /**
     * Whether or not the control key is being held down.
     */
    private boolean ctrlDown = false;

    /**
     * Whether or not the shift key is being held down.
     */
    private boolean shiftDown = false;

    /**
     * Whether or not the meta key is being held down.
     */
    private boolean metaDown = false;

    /**
     * State of ctrlDown from the last time doMouseTracking() was run.
     */
    private boolean mCtrlDown = false;

    /**
     * State of shiftDown from the last time doMouseTracking() was run.
     */
    private boolean mShiftDown = false;

    /**
     * State of metaDown from the last time doMouseTracking() was run.
     */
    private boolean mMetaDown = false;

    /**
     * If a mouse button is held down,
     * whether or not the mouse has moved from its original location when the button was pressed.
     */
    private boolean mouseDiff = false;

    /**
     * The title of the xterm window.
     */
    private String title = "";

    /**
     * xterm's answerbackString resource.
     * Upon receiving a ^E (CTRL+E) character, the terminal sends back a string.
     * By default this string is an empty string,
     * however many terminal emulators send back the name of the terminal emulator,
     * for example PuTTY.
     */
    private String res_answerbackString = "";

    /**
     * xterm's directColor resource.
     * If this is enabled, shells can make xtermView print in any 8-bit colour, rather than only
     * those in the 256 colour palette.
     */
    private boolean res_directColor = true;

    /**
     * xterm's titeInhibit resource.
     * Leave this disabled unless you have a really good reason to enable it.
     * Since we do not use termcap,
     * enabling this essentially prevents using the 47, 1047, 1048 and 1049 private mode controls,
     * which are associated with the Alternate Screen Buffer.
     */
    private boolean res_titeInhibit = false;

    /**
     * Buffer where characters are stored if characters are sent to the terminal
     * before it has fully initialised. This is to prevent crashes.
     */
    private List<Character> preBuffer = new ArrayList<>();

    /**
     * Whether or not the terminal view has been initialised properly.
     */
    private boolean ready = false;
}
