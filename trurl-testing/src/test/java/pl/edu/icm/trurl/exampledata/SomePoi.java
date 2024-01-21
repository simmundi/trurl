/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;
import pl.edu.icm.trurl.io.visnow.VnCoords;

@WithDao
public class SomePoi implements VnCoords {
    private float x;
    private float y;
    private int intAttr;
    private float floatAttr;
    private double doubleAttr;
    private short shortAttribute;
    private boolean booleanAttribute;
    private Color enumAttribute;

    public int getIntAttr() {
        return intAttr;
    }

    public void setIntAttr(int intAttr) {
        this.intAttr = intAttr;
    }

    public float getFloatAttr() {
        return floatAttr;
    }

    public void setFloatAttr(float floatAttr) {
        this.floatAttr = floatAttr;
    }

    public double getDoubleAttr() {
        return doubleAttr;
    }

    public void setDoubleAttr(double doubleAttr) {
        this.doubleAttr = doubleAttr;
    }

    public short getShortAttribute() {
        return shortAttribute;
    }

    public void setShortAttribute(short shortAttribute) {
        this.shortAttribute = shortAttribute;
    }

    public boolean isBooleanAttribute() {
        return booleanAttribute;
    }

    public void setBooleanAttribute(boolean booleanAttribute) {
        this.booleanAttribute = booleanAttribute;
    }

    public Color getEnumAttribute() {
        return enumAttribute;
    }

    public void setEnumAttribute(Color enumAttribute) {
        this.enumAttribute = enumAttribute;
    }

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
