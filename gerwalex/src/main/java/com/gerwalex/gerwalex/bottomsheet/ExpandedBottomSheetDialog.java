package com.gerwalex.gerwalex.bottomsheet;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.WindowManager;

/**
 * Ergaenzung zum BottomSheetDialog gemaess Design-Lib. Das Ausdimmen des Hintergrundes wird
 * unterbunden, kann aber durch {@link ExpandedBottomSheetDialog#setBackgroundDimmed()} wieder
 * eingeschaltet werden.
 */
public class ExpandedBottomSheetDialog extends BottomSheetDialog {
    public ExpandedBottomSheetDialog(@NonNull Context context) {
        super(context);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public ExpandedBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /**
     * Damit kann gesteuert werden, ob der Hintergrund gedimmt werden soll. Default ist false
     */
    public void setBackgroundDimmed() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
