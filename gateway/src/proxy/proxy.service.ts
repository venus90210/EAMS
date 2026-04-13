import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { Request } from 'express';
import { Observable } from 'rxjs';
import { AxiosResponse } from 'axios';

export interface ValidatedUser {
  userId: string;
  role: string;
  institutionId?: string;
}

@Injectable()
export class ProxyService {
  private backendUrl: string;

  constructor(
    private httpService: HttpService,
    private configService: ConfigService,
  ) {
    this.backendUrl = this.configService.get<string>('BACKEND_URL') || 'http://localhost:8080';
  }

  forward(request: Request, user: ValidatedUser): Observable<AxiosResponse> {
    // Strip /api prefix: gateway exposes /api/*, backend has controllers at /*
    // Use originalUrl to preserve query strings
    const pathWithQuery = request.originalUrl.replace(/^\/api/, '');
    const targetUrl = `${this.backendUrl}${pathWithQuery}`;
    console.log(`[ProxyService] ${request.method} -> ${targetUrl}`);

    const headers = { ...request.headers };
    delete headers.authorization;
    delete headers.host;

    // Inject validated user identity for the backend's TenantFilter
    headers['X-User-Id'] = user.userId;
    headers['X-User-Role'] = user.role;
    if (user.institutionId) {
      headers['X-Institution-Id'] = user.institutionId;
    }

    const method = (request.method || 'GET').toUpperCase();
    const config = {
      headers,
    };

    // Use typed method based on HTTP verb
    switch (method) {
      case 'GET':
        return this.httpService.get(targetUrl, config);
      case 'POST':
        return this.httpService.post(targetUrl, request.body, config);
      case 'PUT':
        return this.httpService.put(targetUrl, request.body, config);
      case 'PATCH':
        return this.httpService.patch(targetUrl, request.body, config);
      case 'DELETE':
        return this.httpService.delete(targetUrl, config);
      case 'HEAD':
        return this.httpService.head(targetUrl, config);
      case 'OPTIONS':
      default:
        return this.httpService.get(targetUrl, config);
    }
  }
}
