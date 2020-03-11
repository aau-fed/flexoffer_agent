/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-entities
 * Copyright (c) 2018 The GoFlex Consortium
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 3/23/18 1:14 PM
 */

package org.goflex.wp2.core.util;

import com.google.gson.Gson;
import org.goflex.wp2.core.entities.FlexOffer;

import javax.persistence.AttributeConverter;

/**
 * Created by bijay on 3/23/18.
 */
public class FlexOfferJsonConvertor implements
        AttributeConverter<FlexOffer, String> {

    @Override
    public String convertToDatabaseColumn(FlexOffer flexOffer) {
        Gson parser = new Gson();
        return parser.toJson(flexOffer, FlexOffer.class);
    }

    @Override
    public FlexOffer convertToEntityAttribute(String source) {
        Gson parser = new Gson();
        return parser.fromJson(source, FlexOffer.class);
    }
}
