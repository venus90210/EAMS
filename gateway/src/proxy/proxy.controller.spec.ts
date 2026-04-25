import { Test, TestingModule } from '@nestjs/testing';
import { ProxyController } from './proxy.controller';
import { ProxyService } from './proxy.service';
import { of, throwError } from 'rxjs';

describe('ProxyController', () => {
  let controller: ProxyController;
  let proxyService: ProxyService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [ProxyController],
      providers: [
        {
          provide: ProxyService,
          useValue: {
            forward: jest.fn(),
          },
        },
      ],
    }).compile();

    controller = module.get<ProxyController>(ProxyController);
    proxyService = module.get<ProxyService>(ProxyService);
  });

  describe('proxy', () => {
    it('should forward request and return backend response', async () => {
      const req = {
        user: { userId: 'user-123', role: 'TEACHER', institutionId: 'inst-456' },
        path: '/api/activities',
        method: 'GET',
        headers: { authorization: 'Bearer token' },
        body: {},
      };

      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      };

      (proxyService.forward as jest.Mock).mockReturnValueOnce(
        of({ status: 200, data: { id: 1, name: 'Test Activity' } }),
      );

      await controller.proxy(req as any, res as any);

      expect(res.status).toHaveBeenCalledWith(200);
      expect(res.json).toHaveBeenCalledWith({ id: 1, name: 'Test Activity' });
    });

    it('should handle proxy service errors', async () => {
      const req = {
        user: { userId: 'user-123', role: 'TEACHER' },
        path: '/api/activities',
        method: 'GET',
        headers: {},
        body: {},
      };

      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      };

      const error = {
        response: {
          status: 404,
          data: { error: 'NOT_FOUND', message: 'Activity not found' },
        },
      };

      (proxyService.forward as jest.Mock).mockReturnValueOnce(throwError(() => error));

      await controller.proxy(req as any, res as any);

      expect(res.status).toHaveBeenCalledWith(404);
      expect(res.json).toHaveBeenCalledWith({ error: 'NOT_FOUND', message: 'Activity not found' });
    });

    it('should return 500 when error has no response status', async () => {
      const req = {
        user: { userId: 'user-123', role: 'TEACHER' },
        path: '/api/activities',
        method: 'GET',
        headers: {},
        body: {},
      };

      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      };

      const error = new Error('Connection timeout');

      (proxyService.forward as jest.Mock).mockReturnValueOnce(throwError(() => error));

      await controller.proxy(req as any, res as any);

      expect(res.status).toHaveBeenCalledWith(500);
      expect(res.json).toHaveBeenCalledWith({
        error: 'GATEWAY_ERROR',
        message: 'Connection timeout',
      });
    });
  });
});
