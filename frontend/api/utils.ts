/**
 * Encode a string as RFC3986 URI component.
 * @param uriComponent The value to encode
 * @returns the encoded URI component
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent?retiredLocale=de#encoding_for_rfc3986
 */
export function encodeRFC3986URIComponent(str: string): string {
  return encodeURIComponent(str).replace(/[!'()*]/g, (c) => `%${c.charCodeAt(0).toString(16).toUpperCase()}`);
}
