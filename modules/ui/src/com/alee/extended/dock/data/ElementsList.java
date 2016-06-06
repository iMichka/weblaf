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

package com.alee.extended.dock.data;

import com.alee.extended.dock.WebDockablePane;
import com.alee.painter.decoration.states.Orientation;
import com.alee.utils.CompareUtils;
import com.alee.utils.TextUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dockable structure element representing either horizontal or vertical list of other structure elements.
 *
 * @author Mikle Garin
 */

@XStreamAlias ( "ElementsList" )
public class ElementsList extends AbstractStructureElement implements StructureContainer
{
    /**
     * List orientation.
     */
    @XStreamAsAttribute
    protected Orientation orientation;

    /**
     * List of structure elements.
     */
    @XStreamImplicit
    protected List<StructureElement> elements;

    /**
     * Constructs new elements list.
     *
     * @param orientation list orientation
     * @param elements    elements to add
     */
    public ElementsList ( final Orientation orientation, final StructureElement... elements )
    {
        super ( TextUtils.generateId ( "EL" ) );
        setOrientation ( orientation );
        for ( final StructureElement element : elements )
        {
            add ( getElementCount (), element );
        }
    }

    @Override
    public void added ( final StructureContainer parent )
    {
        super.added ( parent );

        // Initializing children
        if ( elements != null )
        {
            for ( final StructureElement element : elements )
            {
                element.added ( this );
            }
        }
    }

    @Override
    public boolean isContent ()
    {
        if ( elements != null )
        {
            for ( final StructureElement element : elements )
            {
                if ( element.isContent () )
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Orientation getOrientation ()
    {
        return orientation;
    }

    @Override
    public void setOrientation ( final Orientation orientation )
    {
        this.orientation = orientation;
    }

    @Override
    public int getElementCount ()
    {
        return elements != null ? elements.size () : 0;
    }

    @Override
    public <E extends StructureElement> E get ( final String id )
    {
        StructureElement element = null;
        if ( elements != null )
        {
            for ( final StructureElement e : elements )
            {
                if ( CompareUtils.equals ( id, e.getId () ) )
                {
                    element = e;
                    break;
                }
                if ( e instanceof StructureContainer )
                {
                    element = ( ( StructureContainer ) e ).get ( id );
                    if ( element != null )
                    {
                        break;
                    }
                }
            }
        }
        return ( E ) element;
    }

    @Override
    public boolean contains ( final String id )
    {
        return get ( id ) != null;
    }

    @Override
    public int indexOf ( final StructureElement element )
    {
        return elements != null ? elements.indexOf ( element ) : -1;
    }

    @Override
    public StructureElement get ( final int index )
    {
        return elements != null ? elements.get ( index ) : null;
    }

    @Override
    public void add ( final int index, final StructureElement element )
    {
        // Ensure elements list is created
        if ( elements == null )
        {
            elements = new ArrayList<StructureElement> ( 2 );
        }

        // Add element
        elements.add ( index, element );
        element.added ( this );
    }

    @Override
    public void remove ( final StructureElement element )
    {
        if ( elements != null )
        {
            elements.remove ( element );
            element.removed ( this );
        }
    }

    @Override
    public boolean isVisible ( final WebDockablePane dockablePane )
    {
        if ( elements != null )
        {
            for ( final StructureElement element : elements )
            {
                if ( element.isVisible ( dockablePane ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void layout ( final WebDockablePane dockablePane, final Rectangle bounds, final List<ResizeData> resizeableAreas )
    {
        // Saving bounds
        setBounds ( bounds );

        // Placing elements
        if ( elements != null )
        {
            final boolean hor = orientation == Orientation.horizontal;

            // Calculating existing sizes
            int summ = 0;
            int cindex = -1;
            final List<StructureElement> visible = new ArrayList<StructureElement> ( elements.size () );
            final List<Integer> sizes = new ArrayList<Integer> ( elements.size () );
            for ( final StructureElement element : elements )
            {
                if ( element.isVisible ( dockablePane ) )
                {
                    final int w = hor ? element.getSize ().width : element.getSize ().height;
                    if ( element.isContent () )
                    {
                        cindex = sizes.size ();
                    }
                    visible.add ( element );
                    sizes.add ( w );
                    summ += w;
                }
            }

            // Continue only if there are visible elements
            if ( visible.size () > 0 )
            {
                // Add spacings
                final int spacing = dockablePane.getContentSpacing ();
                final int spacings = spacing * ( visible.size () - 1 );
                summ += spacings;

                // Adjusting sizes if they do not fit into available area
                final int totalSpace = hor ? bounds.width : bounds.height;
                if ( summ > totalSpace )
                {
                    // Shrinking all elements according to their size
                    final int available = totalSpace - spacings;
                    for ( int i = 0; i < visible.size (); i++ )
                    {
                        sizes.set ( i, ( int ) Math.floor ( ( float ) available * sizes.get ( i ) / ( summ - spacings ) ) );
                    }
                }
                else if ( summ < totalSpace )
                {
                    if ( cindex != -1 )
                    {
                        // Filling all available space with content
                        sizes.set ( cindex, totalSpace - summ + sizes.get ( cindex ) );
                    }
                    else
                    {
                        // Stretching elements across the area since there is no content in this container
                        final int available = totalSpace - spacings;
                        for ( int i = 0; i < visible.size (); i++ )
                        {
                            sizes.set ( i, ( int ) Math.floor ( ( float ) available * sizes.get ( i ) / ( summ - spacings ) ) );
                        }
                    }
                }

                // Placing structure elements
                int x = bounds.x;
                int y = bounds.y;
                for ( int i = 0; i < visible.size (); i++ )
                {
                    final StructureElement element = visible.get ( i );

                    // Placing element
                    final int width = hor ? sizes.get ( i ) : bounds.width;
                    final int height = hor ? bounds.height : sizes.get ( i );
                    element.layout ( dockablePane, new Rectangle ( x, y, width, height ), resizeableAreas );

                    // Adding resize element bounds
                    if ( i < visible.size () - 1 )
                    {
                        final int rg = dockablePane.getResizeGripper ();
                        final int rgx = hor ? x + width + spacing / 2 - rg / 2 : x;
                        final int rgy = hor ? y : y + height + spacing / 2 - rg / 2;
                        final int rgw = hor ? rg : width;
                        final int rgh = hor ? height : rg;
                        final Rectangle rb = new Rectangle ( rgx, rgy, rgw, rgh );
                        resizeableAreas.add ( new ResizeData ( rb, orientation, element.getId (), visible.get ( i + 1 ).getId () ) );
                    }

                    // Incrementing coordinate
                    if ( hor )
                    {
                        x += width + spacing;
                    }
                    else
                    {
                        y += height + spacing;
                    }
                }
            }
        }
    }

    @Override
    public Dimension getMinimumSize ( final WebDockablePane dockablePane )
    {
        // Base minimum size
        Dimension min = dockablePane.getMinimumElementSize ();

        // Children-dictated minimum size
        if ( dockablePane.isOccupyMinimumSizeForChildren () )
        {
            final Dimension mc = new Dimension ( 0, 0 );
            if ( elements != null )
            {
                final boolean hor = orientation == Orientation.horizontal;
                final int spacing = dockablePane.getContentSpacing ();
                for ( final StructureElement element : elements )
                {
                    final Dimension minElement = element.getMinimumSize ( dockablePane );
                    if ( hor )
                    {
                        mc.width += minElement.width + spacing;
                        mc.height = Math.max ( minElement.height, mc.height );
                    }
                    else
                    {
                        mc.width = Math.max ( minElement.width, mc.width );
                        mc.height += minElement.height + spacing;
                    }
                }
                if ( hor )
                {
                    mc.width -= spacing;
                }
                else
                {
                    mc.height -= spacing;
                }
            }
            min = new Dimension ( Math.max ( min.width, mc.width ), Math.max ( min.height, mc.height ) );
        }

        // Validating size
        // This is made here to optimize performance
        if ( size.width < min.width || size.height < min.height )
        {
            setSize ( new Dimension ( Math.max ( size.width, min.width ), Math.max ( size.height, min.height ) ) );
        }

        return min;
    }
}