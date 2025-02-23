import { Response } from 'miragejs';

export function ok<T extends object | null>(data: T): Response {
  return new Response(200, {}, JSON.stringify(data));
}

export function parseParams<T>(data: string): T {
  const params = new URLSearchParams(data);
  const result: Record<string, string> = {};
  for (const [key, value] of params.entries()) {
    result[key] = value;
  }
  return result as T;
}
