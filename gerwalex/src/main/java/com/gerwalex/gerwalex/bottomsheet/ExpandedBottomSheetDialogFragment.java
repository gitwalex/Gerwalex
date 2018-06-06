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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;

/**
 * Ergaenzung zum BottomSheetDialogFragment gemaess Design-Lib. Zeigt bei {@link
 * ExpandedBottomSheetDialogFragment#onStart()} gleich das Sheet mittels Behavior.STATE_EXPAND.
 */
public class ExpandedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private ExpandedBottomSheetDialog dlg;
    private boolean isDimmed;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dlg = new ExpandedBottomSheetDialog(getActivity(), getTheme());
        if (isDimmed) {
            dlg.setBackgroundDimmed();
        }
        return dlg;
    }

    /**
     * Damit kann gesteuert werden, ob der Hintergrund gedimmt werden soll. Default ist false
     */
    public void setBackgroundDimmed() {
        if (dlg != null) {
            dlg.setBackgroundDimmed();
        }
        isDimmed = true;
    }
}

