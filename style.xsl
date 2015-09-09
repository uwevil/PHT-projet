<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output 
method="html"
encoding="UTF-8"
doctype-public="-//W3C//DTD XHTML//EN"
doctype-system="http://www.w3.org/TR/2001/REC-xhtml11-20010531"
indent="yes" />

<xsl:template match="/">
<html>
<body>
    <h2> REQUEST </h2>
    <p>
        <b>AtTime</b> est le temps en miliseconde à partir du lancement de la requête qu'on trouve le résultat.
        Dans le cas qu'il y a plusieurs de réponses, ce temps est toujours calculé à partir du lancement de la requête. Pour trouver le temps entre 2 résultats, il vous faut de faire la soustraction entre 2 temps.
       <p> 
           <b>RetrievedAt</b> est le nombre de <i>retrieves</i> pour arriver au résultat. Dans le cas qu'il y plusieurs de réponses trouvées, ce nombre est toujours calculé à partir du lancement de la requêtes.
        </p>
    </p>
        <table border="1">
            <tr bgcolor="cyan">
                <th style="text-align:right">ID</th>
                <th style="text-align:right">NbKeywords</th>
          <!--      <th style="text-align:right">Keywords</th> -->
                <th style="text-align:center">Results
                    <table border="1px" >
                        <tr>
                            <td>AtTime</td>
                            <td>RetrievedAt</td>
                        </tr>
                    </table></th>
                <th style="text-align:right">NbGetStatus</th>
                <th style="text-align:right">NbRetrieves</th>
                <th style="text-align:right">totalFiltersRetrieved</th>
            </tr>
            <xsl:apply-templates select="*"/>            
        </table>
</body>
</html>
</xsl:template>

<xsl:template match="request">
<tr>    
    <td style="text-align:right"><xsl:value-of select="@id"/></td>
   <td style="text-align:right"><xsl:value-of select="@nbkeywords"/></td>
 <!--   <td style="text-align:right"><xsl:value-of select="keywords"/></td>  -->
    <td>
        <table border="1">
        <xsl:apply-templates select="resultat"/>
        </table>
    </td>
    <xsl:apply-templates select="nbRetrieves"/>
    <xsl:apply-templates select="nbGetStatus"/>
    <xsl:apply-templates select="totalFiltersRetrieved"/>
</tr>
</xsl:template>

<xsl:template match="resultat">
        <tr>
            <td>+<xsl:value-of select="time"/></td>
            <td><xsl:value-of select="retrieveAt"/></td>
        </tr>
</xsl:template>


<xsl:template match="nbRetrieves">
    <td style="text-align:right"><xsl:value-of select="."/></td>
</xsl:template>

<xsl:template match="nbGetStatus">
    <td style="text-align:right"><xsl:value-of select="."/></td>
</xsl:template>

<xsl:template match="totalFiltersRetrieved">
    <td style="text-align:right"><xsl:value-of select="."/></td>
</xsl:template>

<xsl:template match="keywords">
</xsl:template>

<xsl:template match="requestBF">
</xsl:template>

<xsl:template match="requestKey">
</xsl:template>

<xsl:template match="path">
</xsl:template>

</xsl:stylesheet>
