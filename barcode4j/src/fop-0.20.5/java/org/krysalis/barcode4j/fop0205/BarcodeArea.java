/*
 * $Id: BarcodeArea.java,v 1.1 2003-12-13 20:23:43 jmaerki Exp $
 * ============================================================================
 * The Krysalis Patchy Software License, Version 1.1_01
 * Copyright (c) 2003 Nicola Ken Barozzi.  All rights reserved.
 *
 * This Licence is compatible with the BSD licence as described and
 * approved by http://www.opensource.org/, and is based on the
 * Apache Software Licence Version 1.1.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed for project
 *        Krysalis (http://www.krysalis.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Krysalis" and "Nicola Ken Barozzi" and
 *    "Barcode4J" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact nicolaken@krysalis.org.
 *
 * 5. Products derived from this software may not be called "Krysalis"
 *    or "Barcode4J", nor may "Krysalis" appear in their name,
 *    without prior written permission of Nicola Ken Barozzi.
 *
 * 6. This software may contain voluntary contributions made by many
 *    individuals, who decided to donate the code to this project in
 *    respect of this licence, and was originally created by
 *    Jeremias Maerki <jeremias@maerki.org>.
 *
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE KRYSALIS PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
package org.krysalis.barcode4j.fop0205;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.render.ps.PSRenderer;
import org.apache.fop.svg.SVGArea;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.output.BarcodeCanvasSetupException;
import org.krysalis.barcode4j.output.svg.SVGCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Class representing an Barcode area in which the Barocde graphics sit
 */
public class BarcodeArea extends Area {
    
    private BarcodeGenerator bargen;
    private String msg;
    private String renderMode;

    /**
     * Construct an Barcode area
     *
     * @param fontState the font state
     * @param width the width of the area
     * @param height the height of the area
     */
    public BarcodeArea(FontState fontState, float width, float height) {
        super(fontState, (int)width, (int)height);
        currentHeight = (int)height;
        contentRectangleWidth = (int)width;
    }

    public void setBarcode(BarcodeGenerator bargen, 
                String msg, String renderMode) {
        this.bargen = bargen;
        this.msg = msg;
        this.renderMode = renderMode;
    }

    public BarcodeGenerator getBarcodeGenerator() {
        return this.bargen;
    }

    public String getMessage() {
        return this.msg;
    }

    public String getRenderMode() {
        return this.renderMode;
    }
    
    public int getWidth() {
        return contentRectangleWidth;
    }

    public double mpt2mm(double mpt) {
        return UnitConv.pt2mm(mpt / 1000);
    }

    /**
     * Render the Barcode.
     *
     * @param renderer the Renderer to use
     */
    public void render(Renderer renderer) {
        if (renderer instanceof PSRenderer) {
            PSRenderer psr = (PSRenderer)renderer;
            renderPostScriptBarcodeUsingSVG(psr);
        } else if (renderer instanceof PDFRenderer) {
            PDFRenderer pdfr = (PDFRenderer)renderer;
            renderPDFBarcodeUsingSVG(pdfr);
        } else {
            MessageHandler.errorln(
                "Cannot render barcode. Unsupported renderer: " 
                    + renderer.getClass().getName());
        }
    }
    
    protected SVGArea createSVGArea() throws BarcodeCanvasSetupException {
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        SVGCanvasProvider svgout = new SVGCanvasProvider(domImpl, true);
        getBarcodeGenerator().generateBarcode(svgout, getMessage());
        Document dom = svgout.getDOM();
        SVGArea svgarea = new SVGArea(getFontState(), getWidth(), getHeight());
        svgarea.setSVGDocument(dom);
        return svgarea;
    }
    
    protected void renderPostScriptBarcodeUsingSVG(PSRenderer psr) {
        try {
            psr.renderSVGArea(createSVGArea());
        } catch (BarcodeCanvasSetupException bcse) {
            MessageHandler.errorln(
                "Couldn't render barcode due to BarcodeCanvasSetupException: " 
                    + bcse.getMessage());
        }
    }

    protected void renderPDFBarcodeUsingSVG(PDFRenderer pdfr) {
        try {
            pdfr.renderSVGArea(createSVGArea());
        } catch (BarcodeCanvasSetupException bcse) {
            MessageHandler.errorln(
                "Couldn't render barcode due to BarcodeCanvasSetupException: " 
                    + bcse.getMessage());
        }
    }
    
}