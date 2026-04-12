import { Test, TestingModule } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { ProxyService } from './proxy.service';
import { of, throwError } from 'rxjs';
import { Request } from 'express';
import { AxiosError } from 'axios';

describe('ProxyService', () => {
  let service: ProxyService;
  let httpService: HttpService;
  let configService: ConfigService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ProxyService,
        {
          provide: HttpService,
          useValue: {
            request: jest.fn(),
          },
        },
        {
          provide: ConfigService,
          useValue: {
            get: jest.fn().mockReturnValue('http://localhost:8080'),
          },
        },
      ],
    }).compile();

    service = module.get<ProxyService>(ProxyService);
    httpService = module.get<HttpService>(HttpService);
    configService = module.get<ConfigService>(ConfigService);
  });

  describe('forward', () => {
    let request: Partial<Request>;
    const user = {
      userId: 'user-123',
      role: 'TEACHER',
      institutionId: 'inst-456',
    };

    beforeEach(() => {
      request = {
        method: 'GET',
        path: '/api/activities',
        headers: {
          authorization: 'Bearer token',
          'user-agent': 'jest',
        },
        body: {},
      };
    });

    it('should inject X-User-Id header', (done) => {
      (httpService.request as jest.Mock).mockReturnValueOnce(of({ status: 200, data: {} }));

      service.forward(request as Request, user).subscribe(() => {
        const callConfig = (httpService.request as jest.Mock).mock.calls[0][0];
        expect(callConfig.headers['X-User-Id']).toBe('user-123');
        done();
      });
    });

    it('should inject X-Institution-Id header', (done) => {
      (httpService.request as jest.Mock).mockReturnValueOnce(of({ status: 200, data: {} }));

      service.forward(request as Request, user).subscribe(() => {
        const callConfig = (httpService.request as jest.Mock).mock.calls[0][0];
        expect(callConfig.headers['X-Institution-Id']).toBe('inst-456');
        done();
      });
    });

    it('should strip Authorization header', (done) => {
      (httpService.request as jest.Mock).mockReturnValueOnce(of({ status: 200, data: {} }));

      service.forward(request as Request, user).subscribe(() => {
        const callConfig = (httpService.request as jest.Mock).mock.calls[0][0];
        expect(callConfig.headers.authorization).toBeUndefined();
        done();
      });
    });

    it('should forward request with correct method and path', (done) => {
      (httpService.request as jest.Mock).mockReturnValueOnce(of({ status: 200, data: {} }));

      service.forward(request as Request, user).subscribe(() => {
        const callConfig = (httpService.request as jest.Mock).mock.calls[0][0];
        expect(callConfig.method).toBe('get');
        expect(callConfig.url).toBe('http://localhost:8080/api/activities');
        done();
      });
    });

    it('should handle POST requests with body', (done) => {
      (request as any).method = 'POST';
      (request as any).body = { name: 'Test Activity' };

      (httpService.request as jest.Mock).mockReturnValueOnce(of({ status: 201, data: { id: 1 } }));

      service.forward(request as Request, user).subscribe(() => {
        const callConfig = (httpService.request as jest.Mock).mock.calls[0][0];
        expect(callConfig.method).toBe('post');
        expect(callConfig.data).toEqual({ name: 'Test Activity' });
        done();
      });
    });

    it('should handle backend 4xx errors', (done) => {
      const axiosError = {
        response: { status: 404, data: { error: 'NOT_FOUND', message: 'Activity not found' } },
      } as any;

      (httpService.request as jest.Mock).mockReturnValueOnce(throwError(() => axiosError));

      service.forward(request as Request, user).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBe(axiosError);
          done();
        },
      });
    });

    it('should handle backend 5xx errors', (done) => {
      const axiosError = {
        response: { status: 500, data: { error: 'SERVER_ERROR' } },
      } as any;

      (httpService.request as jest.Mock).mockReturnValueOnce(throwError(() => axiosError));

      service.forward(request as Request, user).subscribe({
        next: () => {
          fail('Should have thrown error');
        },
        error: (error) => {
          expect(error).toBe(axiosError);
          done();
        },
      });
    });
  });
});
