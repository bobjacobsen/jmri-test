// InstanceInitializer.java

package jmri;

/**
 * Interface providing initialization of specific objects
 * by default.  This is used to move references to 
 * specific subtypes out of the jmri package and into 
 * e.g. jmri.managers.
 
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2010
 * @version			$Revision: 1.1 $
 * @since           2.9.4
 */
public interface InstanceInitializer {

    public <T> Object getDefault(Class<T> type);
    
}

/* @(#)InstanceInitializer.java */
