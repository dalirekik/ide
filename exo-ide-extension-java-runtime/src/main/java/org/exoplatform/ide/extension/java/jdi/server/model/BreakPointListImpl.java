/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.exoplatform.ide.extension.java.jdi.server.model;

import org.exoplatform.ide.extension.java.jdi.shared.BreakPoint;
import org.exoplatform.ide.extension.java.jdi.shared.BreakPointList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class BreakPointListImpl implements BreakPointList {
    private List<BreakPoint> breakPoints;

    public BreakPointListImpl(List<BreakPoint> breakPoints) {
        this.breakPoints = breakPoints;
    }

    @Override
    public List<BreakPoint> getBreakPoints() {
        if (breakPoints == null) {
            breakPoints = new ArrayList<BreakPoint>();
        }
        return breakPoints;
    }

    @Override
    public void setBreakPoints(List<BreakPoint> breakPoints) {
        this.breakPoints = breakPoints;
    }
}
