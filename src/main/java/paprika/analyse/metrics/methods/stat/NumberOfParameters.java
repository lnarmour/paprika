/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika.analyse.metrics.methods.stat;

import paprika.analyse.entities.PaprikaMethod;
import paprika.analyse.metrics.UnaryMetric;
import soot.SootMethod;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class NumberOfParameters implements MethodStatistic {

    public static final String NAME = "number_of_parameters";

    @Override
    public void collectMetric(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        UnaryMetric<Integer> metric = new UnaryMetric<>(NAME, paprikaMethod,
                sootMethod.getParameterCount());
        metric.updateEntity();
    }

}
