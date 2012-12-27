/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Named;

import java.util.Comparator;

public class NamedComparatorByLetterCase implements Comparator<Named> {
    public static final NamedComparator INSTANCE = new NamedComparator();

    public int compare(Named named, Named named1) {
        return named.getName().toLowerCase().compareTo(named1.getName().toLowerCase());
    }
}
