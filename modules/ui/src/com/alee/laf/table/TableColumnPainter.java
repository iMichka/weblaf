/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.laf.table;

import com.alee.painter.decoration.AbstractSectionDecorationPainter;
import com.alee.painter.decoration.DecorationState;
import com.alee.painter.decoration.IDecoration;

import javax.swing.*;
import java.util.List;

/**
 * Simple table column painter based on {@link com.alee.painter.decoration.AbstractDecorationPainter}.
 *
 * @param <E> component type
 * @param <U> component UI type
 * @param <D> decoration type
 * @author Mikle Garin
 */

public class TableColumnPainter<E extends JTable, U extends WebTableUI, D extends IDecoration<E, D>>
        extends AbstractSectionDecorationPainter<E, U, D> implements ITableColumnPainter<E, U>
{
    /**
     * Painted column index.
     */
    protected int column;

    @Override
    protected boolean isFocused ()
    {
        return false;
    }

    @Override
    protected List<String> getDecorationStates ()
    {
        final List<String> states = super.getDecorationStates ();
        if ( !component.getRowSelectionAllowed () && component.getColumnSelectionAllowed () && component.isColumnSelected ( column ) )
        {
            states.add ( DecorationState.selected );
        }
        states.add ( column % 2 == 0 ? DecorationState.even : DecorationState.odd );
        return states;
    }

    @Override
    public void prepareToPaint ( final int column )
    {
        this.column = column;
        updateDecorationState ();
    }
}